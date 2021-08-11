package no.nav.pensjon.pdfmerger

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.features.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo
import org.apache.pdfbox.pdmodel.PDDocument.load
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class AdvancedPdfMergerTest {
    private val pdfMerger = PdfMerger().apply {
        bindTo(SimpleMeterRegistry())
    }
    private val objectMapper = jsonMapper {
        addModule(kotlinModule())
        addModule(JavaTimeModule())
    }

    private val documentA = readTestResource("/a.pdf")!!.readBytes()
    private val documentB = readTestResource("/b.pdf")!!.readBytes()
    private val documentVedleggA = readTestResource("/vedleggA.pdf")!!.readBytes()
    private val documentVedleggB = readTestResource("/vedleggB.pdf")!!.readBytes()
    private val invalidDocument = readTestResource("/not_a_pdf")!!.readBytes()

    @Test
    fun testMergeTwoHoveddokument() {
        val mergedDocument = pdfMerger.mergeWithSeparator(
            createInfo(),
            mutableMapOf("a.pdf" to documentA, "b.pdf" to documentB)
        )

        assertEquals(
            expected = load(documentA).numberOfPages + load(documentB).numberOfPages + 3,
            actual = load(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents + one frontpage and two separatorpages"
        )
    }

    @Test
    fun testMergeTwoHoveddokumentOneWithVedlegg() {
        val mergedDocument = pdfMerger.mergeWithSeparator(
            createInfoWithVedlegg(),
            mutableMapOf(
                "a.pdf" to documentA,
                "b.pdf" to documentB,
                "vedleggA.pdf" to documentVedleggA
            )
        )

        assertEquals(
            expected = load(documentA).numberOfPages + load(documentVedleggA).numberOfPages +
                load(documentB).numberOfPages + 3,
            actual = load(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents + one frontpage and two separatorpages"
        )
    }

    @Test
    fun testMergeTwoHoveddokumentOneWithTwoVedlegg() {
        val mergedDocument = pdfMerger.mergeWithSeparator(
            createInfoWithTwoVedlegg(),
            mutableMapOf(
                "a.pdf" to documentA,
                "b.pdf" to documentB,
                "vedleggA.pdf" to documentVedleggA,
                "vedleggB.pdf" to documentVedleggB
            )
        )

        assertEquals(
            expected = load(documentA).numberOfPages + load(documentVedleggA).numberOfPages +
                load(documentVedleggB).numberOfPages + load(documentB).numberOfPages + 3,
            actual = load(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents + one frontpage and two separatorpages"
        )
    }

    @Test
    fun testMergeTwoVedleggNoHoveddokument() {
        val mergedDocument = pdfMerger.mergeWithSeparator(
            createInfoWithTwoVedleggWithoutHoveddokument(),
            mutableMapOf("vedleggA.pdf" to documentVedleggA, "vedleggB.pdf" to documentVedleggB)
        )

        assertEquals(
            expected = load(documentVedleggA).numberOfPages +
                load(documentVedleggB).numberOfPages + 2,
            actual = load(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents + one frontpage and one separatorpage"
        )
    }

    @Test(expected = IOException::class)
    fun test_mergeWithSeparator_of_invalid_document_throws_IOException() {
        pdfMerger.mergeWithSeparator(
            createInfoWithBadFile(),
            mutableMapOf(
                "a.pdf" to documentA,
                "not_a_pdf" to invalidDocument
            )
        )
    }

    @Test(expected = BadRequestException::class)
    fun test_mergeWithSeparator_with_missing_file_throws_BadRequestExtection() {
        pdfMerger.mergeWithSeparator(createInfo(), mutableMapOf("a.pdf" to documentA))
    }

    private fun createInfo() = mergeInfoFromResource("/mergerequest.json")

    private fun createInfoWithBadFile() = mergeInfoFromResource("/mergerequestWithBadFile.json")

    private fun createInfoWithVedlegg() = mergeInfoFromResource("/mergerequestWithVedlegg.json")

    private fun createInfoWithTwoVedlegg() = mergeInfoFromResource("/mergerequestWithTwoVedlegg.json")

    private fun createInfoWithTwoVedleggWithoutHoveddokument() =
        mergeInfoFromResource("/mergerequestTwoVedleggWithoutHoveddokument.json")

    private fun mergeInfoFromResource(name: String): MergeInfo =
        javaClass.getResource(name)
            ?.run {
                objectMapper.readValue(readText(charset("UTF-8")))
            }
            ?: throw RuntimeException("Could not find resource with name $name")

    private fun readTestResource(name: String) =
        javaClass.getResourceAsStream(name)
}
