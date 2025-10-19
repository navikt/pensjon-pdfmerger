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
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationAdvancedMergeExceptionTest {
    private val documentA = getResource("/a.pdf").readBytes()
    private val mergeRequest = getResource("/mergerequestOnlyRequiresFildsAndAHoveddokument.json")
        .readText(charset("UTF-8"))

    @Test
    fun `posting request without documents to the advanced merge endpoint`() = testApplication {
        application {
            main()
        }

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
                        append("mergeinfo", mergeRequest)
                    },
                    boundary = boundary
                )
            )
        }.apply {
            assertEquals(
                expected = InternalServerError,
                actual = status
            )
            assertTrue(bodyAsText().contains("Missing documents"))
        }
    }

    @Test
    fun `posting request without mergeinfo to the advanced merge endpoint`() = testApplication {
        application {
            main()
        }


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
                    },
                    boundary = boundary
                )
            )
        }.apply {
            assertEquals(
                expected = InternalServerError,
                actual = status
            )
            assertTrue(bodyAsText().contains("Missing merge info"))
        }
    }

    @Test
    fun `posting request with duplicate documents to the advanced merge endpoint`() = testApplication {
        application {
            main()
        }

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
                        append("mergeinfo", mergeRequest)
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
                            "a_again",
                            documentA,
                            Headers.build {
                                append(ContentDisposition, File
                                    .withParameter(Name, "a_again")
                                    .withParameter(FileName, "a.pdf")
                                    .toString())
                                append(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Pdf.toString())
                            }
                        )
                    },
                    boundary = boundary
                )
            )
        }.apply {
            assertEquals(
                expected = InternalServerError,
                actual = status
            )
            assertTrue(bodyAsText().contains("unique file name"))
        }
    }

    private fun getResource(name: String) = javaClass.getResource(name)
        ?: throw FileNotFoundException("Missing test file '$name'")
}
