package no.nav.pensjon.pdfmerger.routes

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Pdf
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import no.nav.pensjon.pdfmerger.MeteringPdfMerger
import no.nav.pensjon.pdfmerger.advancedMerge.mapRequestToDomain
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo
import no.nav.pensjon.pdfmerger.logger

fun Route.mergeWithSeparator(meteringPdfMerger: MeteringPdfMerger, mapper: JsonMapper) {
    post("/mergeWithSeparator") {
        try {
            var info: MergeInfo? = null
            val documents: MutableMap<String, ByteArray> = mutableMapOf()

            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val filename = part.originalFileName as String
                        if (documents.containsKey(filename)) {
                            throw IllegalArgumentException(
                                "Must have unique file name for documents, " +
                                        "file $filename appears several times"
                            )
                        }
                        val channel = part.provider()
                        documents[filename] = channel.readRemaining().readByteArray()
                    }

                    is PartData.FormItem -> {
                        if (info != null) {
                            throw IllegalArgumentException("Must only send one FormItem with MergeInfo")
                        }

                        info = mapRequestToDomain(mapper.readValue(part.value))
                    }

                    else -> {
                        logger.warn("Don't know how to handle part of type ${part::class}}")
                    }
                }
            }
            require(documents.isNotEmpty()) { "Missing documents to merge" }

            call.respondBytes(
                bytes = meteringPdfMerger.mergeWithSeparator(
                    requireNotNull(info) { "Missing merge info FormItem" },
                    documents.toMap()
                ),
                contentType = Pdf
            )
        } catch (e: Exception) {
            logger.error("Unable to merge pdf documents", e)
            call.response.status(HttpStatusCode.InternalServerError)
            call.respondText { "Unable to merge PDF documents ${e.message}" }
        }
    }
}
