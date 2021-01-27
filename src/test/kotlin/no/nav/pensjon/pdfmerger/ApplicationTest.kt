package no.nav.pensjon.pdfmerger

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentDisposition.Parameters.Name
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.PartData.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import org.apache.pdfbox.pdmodel.PDDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    @Test fun `ping returns 200 status ok`() = withTestApplication(Application::main) {
        with(handleRequest(Get, "/ping")) {
            assertEquals(OK, response.status())
        }
    }

    @Test fun `posting documents to the merge endpoint, returns a merged document and updates the documents metrics`() {
        val documentA: ByteArray = ApplicationTest::class.java.getResourceAsStream("/a.pdf").readBytes()
        val documentB: ByteArray = ApplicationTest::class.java.getResourceAsStream("/b.pdf").readBytes()
        val expectedPageCount = PDDocument.load(documentA).numberOfPages + PDDocument.load(documentB).numberOfPages

        withTestApplication(Application::main) {
            with(
                handleRequest(Post, "/merge") {
                    val boundary = "***bbb***"

                    addHeader(HttpHeaders.ContentType, ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString())
                    setBody(
                        boundary,
                        listOf(
                            FileItem(
                                { documentA.inputStream().asInput() }, {},
                                headersOf(
                                    ContentDisposition,
                                    File
                                        .withParameter(Name, "a")
                                        .withParameter(FileName, "a.pdf")
                                        .toString()
                                )
                            ),
                            FileItem(
                                { ApplicationTest::class.java.getResourceAsStream("/b.pdf").asInput() }, {},
                                headersOf(
                                    ContentDisposition,
                                    File
                                        .withParameter(Name, "b")
                                        .withParameter(FileName, "b.pdf")
                                        .toString()
                                )
                            ),
                        )
                    )
                }
            ) {
                assertEquals(
                    expected = OK,
                    actual = response.status()
                )

                assertEquals(
                    expected = expectedPageCount,
                    message = "Expected the response to have a page count equal to the sum of pages in the input documents",
                    actual = PDDocument.load(response.byteContent).numberOfPages
                )
            }

            with(handleRequest(Get, "/metrics")) {
                assertEquals(OK, response.status())
                assertEquals(expected = documentA.size + documentB.size, actual = getMetric("merger_document_size_bytes_sum").toInt())
                assertTrue { getMetric("merger_merged_document_size_bytes_sum").toInt() > 0 }
                assertEquals(expected = 2, actual = getMetric("merger_document_count_files_sum").toLong())
                assertEquals(expected = 1, actual = getMetric("merger_call_count_calls_total").toLong())
            }
        }
    }

    private fun TestApplicationCall.getMetric(metric: String): String {
        val matchResult = Regex("$metric (\\d+)").find(response.content!!)
        assertNotNull(matchResult, message = "Metric $metric was missing")

        val (size) = matchResult.destructured
        return size
    }
}
