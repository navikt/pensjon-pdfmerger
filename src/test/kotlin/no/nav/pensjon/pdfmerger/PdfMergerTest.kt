package no.nav.pensjon.pdfmerger

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.pdfbox.pdmodel.PDDocument.load
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PdfMergerTest {
    private val pdfMerger = PdfMerger()

    private val a = PdfMergerTest::class.java.getResourceAsStream("/a.pdf").readAllBytes()
    private val b = PdfMergerTest::class.java.getResourceAsStream("/b.pdf").readAllBytes()
    private val invalidDocument = PdfMergerTest::class.java.getResourceAsStream("/not_a_pdf").readAllBytes()

    @BeforeTest
    fun bindMetricsRegistry() {
        pdfMerger.bindTo(SimpleMeterRegistry())
    }

    @Test
    fun testMergeDocuments() {
        val expectedPageCount = load(a).numberOfPages + load(b).numberOfPages

        val result = pdfMerger.mergeDocuments(listOf(a, b))

        assertEquals(expected = expectedPageCount, actual = load(result).numberOfPages)
    }

    @Test(expected = IOException::class)
    fun test_merge_of_invalid_document_throws_IOException() {
        pdfMerger.mergeDocuments(listOf(a, invalidDocument))
    }
}
