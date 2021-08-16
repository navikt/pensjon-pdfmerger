package no.nav.pensjon.pdfmerger

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentDisposition.Parameters.Name
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.PartData.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import org.apache.pdfbox.pdmodel.PDDocument.load
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `ping returns 200 status ok`() = withTestApplication(Application::main) {
        with(handleRequest(Get, "/ping")) {
            assertEquals(OK, response.status())
        }
    }

    /**
     * Posting documents to the /merge endpoint should
     * - return a merged document
     * - update the metrics of the /metrics endpoint
     * The merging is tested by counting the number of pages in the input documents and
     * expecting the merged document to have a page count equal to the sum of pages of the
     * input documents.
     */
    @Test
    fun `posting documents to the merge endpoint`() {
        val documentA = readTestResource("/a.pdf")
        val documentB = readTestResource("/b.pdf")

        withTestApplication(Application::main) {
            with(
                handleRequest(Post, "/merge") {
                    val boundary = "***bbb***"

                    addHeader(
                        name = ContentType,
                        value = FormData.withParameter(name = "boundary", value = boundary)
                            .toString()
                    )
                    setBody(
                        boundary = boundary,
                        parts = listOf(
                            FileItem(
                                provider = { documentA.inputStream().asInput() },
                                dispose = {},
                                partHeaders = headersOf(
                                    name = ContentDisposition,
                                    value = File
                                        .withParameter(Name, "a")
                                        .withParameter(FileName, "a.pdf")
                                        .toString()
                                )
                            ),
                            FileItem(
                                provider = { documentB.inputStream().asInput() },
                                dispose = {},
                                partHeaders = headersOf(
                                    name = ContentDisposition,
                                    value = File
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
                    expected = load(documentA).numberOfPages + load(documentB).numberOfPages,
                    actual = load(response.byteContent).numberOfPages,
                    message = "The merged document should have a page count" +
                        " equal to the sum of pages in the input documents"
                )
            }

            with(handleRequest(Get, "/metrics")) {
                assertEquals(
                    expected = OK,
                    actual = response.status()
                )
                assertTrue { metric("merger_merged_document_size_bytes_sum").toInt() > 0 }
                assertEquals(
                    expected = documentA.size + documentB.size,
                    actual = metric("merger_document_size_bytes_sum").toInt()
                )
                assertEquals(
                    expected = 2,
                    actual = metric("merger_document_count_files_sum").toLong()
                )
                assertEquals(
                    expected = 1,
                    actual = metric("merger_call_count_calls_total").toLong()
                )
            }
        }
    }

    private fun readTestResource(s: String) =
        javaClass.getResourceAsStream(s)!!.readBytes()

    private fun TestApplicationCall.metric(name: String): String {
        val matchResult = Regex("$name (\\d+)").find(response.content!!)
        assertNotNull(matchResult, message = "Metric $name was missing")

        val (size) = matchResult.destructured
        return size
    }
}
