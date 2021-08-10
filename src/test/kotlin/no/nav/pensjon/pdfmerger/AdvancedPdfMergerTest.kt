package no.nav.pensjon.pdfmerger

import io.ktor.features.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.pdfbox.pdmodel.PDDocument.load
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class AdvancedPdfMergerTest {
    private val pdfMerger = PdfMerger().apply {
        bindTo(SimpleMeterRegistry())
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

    private fun createInfo(): MutableList<String> {
        return mutableListOf(
            javaClass.getResource("/mergerequest.json")!!
                .readText(charset("UTF-8"))
        )
    }

    private fun createInfoWithBadFile(): MutableList<String> {
        return mutableListOf(
            javaClass.getResource("/mergerequestWithBadFile.json")!!
                .readText(charset("UTF-8"))
        )
    }

    private fun createInfoWithVedlegg(): MutableList<String> {
        return mutableListOf(
            javaClass.getResource("/mergerequestWithVedlegg.json")!!
                .readText(charset("UTF-8"))
        )
    }

    private fun createInfoWithTwoVedlegg(): MutableList<String> {
        return mutableListOf(
            javaClass.getResource("/mergerequestWithTwoVedlegg.json")!!
                .readText(charset("UTF-8"))
        )
    }

    private fun createInfoWithTwoVedleggWithoutHoveddokument(): MutableList<String> {
        return mutableListOf(
            javaClass.getResource("/mergerequestTwoVedleggWithoutHoveddokument.json")!!
                .readText(charset("UTF-8"))
        )
    }

    private fun readTestResource(name: String) =
        javaClass.getResourceAsStream(name)
}
