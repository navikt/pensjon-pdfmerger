package no.nav.pensjon.pdfmerger

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.pdfbox.Loader.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals

class MeteringPdfMergerTest {
    private val meteringPdfMerger = MeteringPdfMerger().apply {
        bindTo(SimpleMeterRegistry())
    }

    private val documentA = readTestResource("/a.pdf")
    private val documentB = readTestResource("/b.pdf")
    private val invalidDocument = readTestResource("/not_a_pdf")

    @Test
    fun testMergeDocuments() {
        val mergedDocument = meteringPdfMerger.mergeDocuments(listOf(documentA, documentB))

        assertEquals(
            expected = loadPDF(documentA).numberOfPages + loadPDF(documentB).numberOfPages,
            actual = loadPDF(mergedDocument).numberOfPages,
            message = "The merged document should have the same page count as the sum of pages " +
                "of the input documents"
        )
    }

    @Test
    fun test_merge_of_invalid_document_throws_IOException() {
        assertThrows<IOException> { meteringPdfMerger.mergeDocuments(listOf(documentA, invalidDocument)) }
    }

    private fun readTestResource(name: String) =
        javaClass.getResourceAsStream(name)
            ?.readBytes()
            ?: throw RuntimeException("Could now find resource '$name'")
}
