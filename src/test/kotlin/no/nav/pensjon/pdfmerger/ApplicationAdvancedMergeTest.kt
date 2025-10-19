package no.nav.pensjon.pdfmerger

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentDisposition.Parameters.Name
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.*
import io.ktor.utils.io.jvm.javaio.*
import org.apache.pdfbox.Loader.*
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
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
    fun `posting documents to the advanced merge endpoint`() = testApplication {
        application {
            main()
        }

        val documentA = readTestResource("/a.pdf")
        val documentB = readTestResource("/b.pdf")
        val documentVedleggA = readTestResource("/vedleggA.pdf")
        val documentVedleggB = readTestResource("/vedleggB.pdf")
        val mergeRequest = readTestResourceAsText("/mergerequestTwoHoveddokOneWithTwoVedlegg.json")

        client.post("/mergeWithSeparator") {
            val boundary = "***bbb***"

            header(
                key = ContentType,
                value = FormData.withParameter(name = "boundary", value = boundary)
                    .toString()
            )
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "a",
                            documentA,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "a")
                                    .withParameter(FileName, "a.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                        append(
                            "b",
                            documentB,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "b")
                                    .withParameter(FileName, "b.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                        append(
                            "vedleggA",
                            documentVedleggA,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "vedleggA")
                                    .withParameter(FileName, "vedleggA.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                        append(
                            "vedleggB",
                            documentVedleggB,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "vedleggB")
                                    .withParameter(FileName, "vedleggB.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                        append("mergeinfo", mergeRequest)
                    },
                    boundary = boundary
                )
            )
        }.apply {
            assertEquals(
                expected = OK,
                actual = status
            )

            assertEquals(
                expected = loadPDF(documentA).numberOfPages +
                        loadPDF(documentVedleggA).numberOfPages +
                        loadPDF(documentVedleggB).numberOfPages +
                        loadPDF(documentB).numberOfPages + 3,
                actual = loadPDF(RandomAccessReadBuffer(bodyAsChannel().toInputStream())).numberOfPages,
                message = "The merged document should have a page count" +
                        " equal to the sum of pages in the input documents + " +
                        "a frontpage and two separatorpages"
            )
        }

        client.get("/metrics").apply {
            assertEquals(
                expected = OK,
                actual = status
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

    @Test
    fun `posting the smallest request to the advanced merge endpoint`() = testApplication {
        application {
            main()
        }

        val documentA = readTestResource("/a.pdf")
        val mergeRequest = readTestResourceAsText("/mergerequestOnlyRequiresFildsAndAHoveddokument.json")

        client.post("/mergeWithSeparator") {
            val boundary = "***bbb***"

            header(
                key = ContentType,
                value = FormData.withParameter(name = "boundary", value = boundary)
                    .toString()
            )
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "a",
                            documentA,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "a")
                                    .withParameter(FileName, "a.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                        append("mergeinfo", mergeRequest)
                    },
                    boundary = boundary
                )
            )
        }.apply {
            assertEquals(
                expected = OK,
                actual = status
            )

            assertEquals(
                expected = loadPDF(documentA).numberOfPages + 2,
                actual = loadPDF(RandomAccessReadBuffer(bodyAsChannel().toInputStream())).numberOfPages,
                message = "The merged document should have a page count" +
                        " equal to the sum of pages in the input document + " +
                        "a frontpage and one separatorpages"
            )
        }

        client.get("/metrics").apply {
            assertEquals(
                expected = OK,
                actual = status
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

    private fun getResource(name: String) = javaClass.getResource(name)
        ?: throw FileNotFoundException("Missing test file '$name'")

    private fun readTestResource(s: String) = getResource(s).readBytes()

    private fun readTestResourceAsText(filename: String) = getResource(filename).readText(charset("UTF-8"))

    private suspend fun HttpResponse.metric(name: String): String {
        val matchResult = Regex("$name (\\d+)").find(bodyAsText())
        assertNotNull(matchResult, message = "Metric $name was missing")

        val (size) = matchResult.destructured
        return size
    }
}
