package no.nav.pensjon.pdfmerger.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pensjon.pdfmerger.MeteringPdfMerger
import no.nav.pensjon.pdfmerger.logger

fun Route.simpleMerge(meteringPdfMerger: MeteringPdfMerger) {
    post("/merge") {
        try {
            val multipart = call.receiveMultipart()
            val documents = mutableListOf<ByteArray>()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    documents += part.streamProvider().readAllBytes()
                }
                part.dispose()
            }

            val mergedDocument = meteringPdfMerger.mergeDocuments(documents)

            call.respondBytes(bytes = mergedDocument, contentType = ContentType.Application.Pdf)
        } catch (e: Exception) {
            logger.error("Unable to merge pdf documents", e)
            call.respondText(
                text = "Unable to merge PDF documents: ${e.message}",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}
