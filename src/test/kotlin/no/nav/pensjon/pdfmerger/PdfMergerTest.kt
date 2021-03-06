package no.nav.pensjon.pdfmerger

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.pdfbox.pdmodel.PDDocument.load
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class PdfMergerTest {
    private val pdfMerger = PdfMerger().apply {
        bindTo(SimpleMeterRegistry())
    }

    private val documentA = readTestResource("/a.pdf")
    private val documentB = readTestResource("/b.pdf")
    private val invalidDocument = readTestResource("/not_a_pdf")

    @Test
    fun testMergeDocuments() {
        val mergedDocument = pdfMerger.mergeDocuments(listOf(documentA, documentB))

        assertEquals(
            expected = load(documentA).numberOfPages + load(documentB).numberOfPages,
            actual = load(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents"
        )
    }

    @Test(expected = IOException::class)
    fun test_merge_of_invalid_document_throws_IOException() {
        pdfMerger.mergeDocuments(listOf(documentA, invalidDocument))
    }

    private fun readTestResource(name: String) =
        javaClass.getResourceAsStream(name).readBytes()
}
