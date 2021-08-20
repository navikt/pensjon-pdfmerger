package no.nav.pensjon.pdfmerger.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pensjon.pdfmerger.MeteringPdfMerger
import no.nav.pensjon.pdfmerger.logger

fun Route.simpleMerge(meteringPdfMerger: MeteringPdfMerger) {
    post("/merge") {
        try {
            val documents = call.receiveMultipart()
                .readAllParts()
                .filterIsInstance<PartData.FileItem>()
                .map { it.streamProvider().readAllBytes() }

            val mergedDocument = meteringPdfMerger.mergeDocuments(documents)

            call.respondBytes(bytes = mergedDocument, contentType = ContentType.Application.Pdf)
        } catch (e: Exception) {
            logger.error("Unable to merge pdf documents", e)
            call.response.status(HttpStatusCode.InternalServerError)
            call.respondText { "Unable to merge PDF documents ${e.message}" }
        }
    }
}
