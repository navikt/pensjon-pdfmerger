package no.nav.pensjon.pdfmerger

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
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import org.apache.pdfbox.pdmodel.PDDocument.load
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationAdvancedMergeTest {

    /**
     * Posting documents to the /mergeWithSeparator endpoint should
     * - return a new document with a frontpage, separatorpage before each hoveddokument,
     * the hoveddokument and its vedlegg
     * - update the metrics of the /metrics endpoint
     * The merging is tested by counting the number of pages in the input documents and
     * expecting the merged document to have a page count equal to the sum of pages of the
     * input documents + frontpage + separatorpage.
     */
    @Test
    fun `posting documents to the advanced merge endpoint`() {
        val documentA = readTestResource("/a.pdf")
        val documentB = readTestResource("/b.pdf")
        val documentVedleggA = readTestResource("/vedleggA.pdf")
        val documentVedleggB = readTestResource("/vedleggB.pdf")
        val mergeRequest = readTestResourceAsText("/mergerequestTwoHoveddokOneWithTwoVedlegg.json")

        withTestApplication(Application::main) {
            with(
                handleRequest(Post, "/mergeWithSeparator") {
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
                            FileItem(
                                provider = { documentVedleggA.inputStream().asInput() },
                                dispose = {},
                                partHeaders = headersOf(
                                    name = ContentDisposition,
                                    value = File
                                        .withParameter(Name, "vedleggA")
                                        .withParameter(FileName, "vedleggA.pdf")
                                        .toString()
                                )
                            ),
                            FileItem(
                                provider = { documentVedleggB.inputStream().asInput() },
                                dispose = {},
                                partHeaders = headersOf(
                                    name = ContentDisposition,
                                    value = File
                                        .withParameter(Name, "vedleggB")
                                        .withParameter(FileName, "vedleggB.pdf")
                                        .toString()
                                )
                            ),
                            FormItem(
                                value = mergeRequest,
                                dispose = {},
                                partHeaders = headersOf()
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
                    expected = load(documentA).numberOfPages +
                            load(documentVedleggA).numberOfPages +
                            load(documentVedleggB).numberOfPages +
                            load(documentB).numberOfPages + 3,
                    actual = load(response.byteContent).numberOfPages,
                    message = "The merged document should have a page count" +
                            " equal to the sum of pages in the input documents + " +
                            "a frontpage and two separatorpages"
                )
            }

            with(handleRequest(Get, "/metrics")) {
                assertEquals(
                    expected = OK,
                    actual = response.status()
                )
                assertTrue {
                    metric("mergeWithSeparator_merged_document_size_bytes_sum").toInt() > 0
                }
                assertEquals(
                    expected = documentA.size + documentB.size + documentVedleggA.size +
                            documentVedleggB.size,
                    actual = metric("mergeWithSeparator_document_size_bytes_sum").toInt()
                )
                assertEquals(
                    expected = 4,
                    actual = metric("mergeWithSeparator_document_count_files_sum").toLong()
                )
                assertEquals(
                    expected = 1,
                    actual = metric("mergeWithSeparator_call_count_calls_total").toLong()
                )
            }
        }
    }

    @Test
    fun `posting the smallest request to the advanced merge endpoint`() {
        val documentA = readTestResource("/a.pdf")
        val mergeRequest = readTestResourceAsText("/mergerequestOnlyRequiresFildsAndAHoveddokument.json")

        withTestApplication(Application::main) {
            with(
                handleRequest(Post, "/mergeWithSeparator") {
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
                            FormItem(
                                value = mergeRequest,
                                dispose = {},
                                partHeaders = headersOf()
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
                    expected = load(documentA).numberOfPages + 2,
                    actual = load(response.byteContent).numberOfPages,
                    message = "The merged document should have a page count" +
                            " equal to the sum of pages in the input document + " +
                            "a frontpage and one separatorpages"
                )
            }

            with(handleRequest(Get, "/metrics")) {
                assertEquals(
                    expected = OK,
                    actual = response.status()
                )
                assertEquals(
                    expected = 1,
                    actual = metric("mergeWithSeparator_document_count_files_sum").toLong()
                )
                assertEquals(
                    expected = 1,
                    actual = metric("mergeWithSeparator_call_count_calls_total").toLong()
                )
            }
        }
    }

    private fun readTestResource(s: String) =
        javaClass.getResourceAsStream(s)!!.readBytes()

    private fun readTestResourceAsText(filename: String) =
        javaClass.getResource(filename)!!
            .readText(charset("UTF-8"))

    private fun TestApplicationCall.metric(name: String): String {
        val matchResult = Regex("$name (\\d+)").find(response.content!!)
        assertNotNull(matchResult, message = "Metric $name was missing")

        val (size) = matchResult.destructured
        return size
    }
}
