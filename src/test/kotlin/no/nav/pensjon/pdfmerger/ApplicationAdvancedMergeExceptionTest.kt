package no.nav.pensjon.pdfmerger

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.ContentDisposition.Parameters.FileName
import io.ktor.http.ContentDisposition.Parameters.Name
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.content.PartData.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationAdvancedMergeExceptionTest {

    @Test
    fun `posting request without documents to the advanced merge endpoint`() {
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
                            FormItem(
                                value = mergeRequest,
                                dispose = {},
                                partHeaders = headersOf()
                            )
                        )
                    )
                }
            ) {
                assertEquals(
                    expected = InternalServerError,
                    actual = response.status()
                )
                assertTrue(response.content!!.contains("Missing documents"))
            }
        }
    }

    @Test
    fun `posting request without mergeinfo to the advanced merge endpoint`() {
        val documentA = readTestResource("/a.pdf")

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
                            )
                        )
                    )
                }
            ) {
                assertEquals(
                    expected = InternalServerError,
                    actual = response.status()
                )
                assertTrue(response.content!!.contains("Missing merge info"))
            }
        }
    }

    @Test
    fun `posting request with duplicate documents to the advanced merge endpoint`() {
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
                            FormItem(
                                value = mergeRequest,
                                dispose = {},
                                partHeaders = headersOf()
                            ),
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
                                provider = { documentA.inputStream().asInput() },
                                dispose = {},
                                partHeaders = headersOf(
                                    name = ContentDisposition,
                                    value = File
                                        .withParameter(Name, "a_again")
                                        .withParameter(FileName, "a.pdf")
                                        .toString()
                                )
                            )
                        )
                    )
                }
            ) {
                assertEquals(
                    expected = InternalServerError,
                    actual = response.status()
                )
                assertTrue(response.content!!.contains("unique file name"))
            }
        }
    }

    private fun readTestResource(s: String) =
        javaClass.getResourceAsStream(s)!!.readBytes()

    private fun readTestResourceAsText(filename: String) =
        javaClass.getResource(filename)!!
            .readText(charset("UTF-8"))
}
