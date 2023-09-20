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
import io.ktor.http.content.PartData.*
import io.ktor.server.testing.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.utils.io.streams.*
import org.apache.pdfbox.Loader.loadPDF
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `ping returns 200 status ok`() = testApplication {
        application {
            main()
        }

        client.get("/ping").apply {
            assertEquals(OK, status)
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
    fun `posting documents to the merge endpoint`() = testApplication {
        application {
            main()
        }

        val documentA = readTestResource("/a.pdf")
        val documentB = readTestResource("/b.pdf")

        client.post("/merge") {
            val boundary = "***bbb***"

            header(
                key = ContentType,
                value = FormData.withParameter(name = "boundary", value = boundary)
                    .toString()
            )
            setBody(
                MultiPartFormDataContent(
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
            )
        }.apply {
            assertEquals(
                expected = OK,
                actual = status
            )

            assertEquals(
                expected = loadPDF(documentA).numberOfPages + loadPDF(documentB).numberOfPages,
                actual = loadPDF(RandomAccessReadBuffer(bodyAsChannel().toInputStream())).numberOfPages,
                message = "The merged document should have a page count" +
                        " equal to the sum of pages in the input documents"
            )
        }


        client.get("/metrics").apply {
            assertEquals(
                expected = OK,
                actual = status
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

    private fun readTestResource(s: String) =
        javaClass.getResourceAsStream(s)!!.readBytes()

    private suspend fun HttpResponse.metric(name: String): String {
        val matchResult = Regex("$name (\\d+)").find(bodyAsText())
        assertNotNull(matchResult, message = "Metric $name was missing")

        val (size) = matchResult.destructured
        return size
    }
}
