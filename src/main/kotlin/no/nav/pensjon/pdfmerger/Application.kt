package no.nav.pensjon.pdfmerger

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Pdf
import io.ktor.http.content.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.event.Level

val logger: Logger = getLogger(Application::class.java)

fun Application.main() {
    val pdfMerger = PdfMerger()
    val appMicrometerRegistry = PrometheusMeterRegistry(DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry

        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            pdfMerger
        )
    }

    install(CallLogging) {
        mdc("application-id") { call ->
            call.request.headers["x-application-id"]
        }

        mdc("correlation-id", ::correlationId)
        mdc("transaction", ::correlationId)

        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        get("/ping") {
            call.respondText { "PONG" }
        }

        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }

        post("/merge") {
            try {
                val documents = call.receiveMultipart()
                    .readAllParts()
                    .filterIsInstance<PartData.FileItem>()
                    .map { it.streamProvider().readAllBytes() }

                val mergedDocument = pdfMerger.mergeDocuments(documents)

                call.respondBytes(bytes = mergedDocument, contentType = Pdf)
            } catch (e: Exception) {
                logger.error("Unable to merge pdf documents", e)
                call.response.status(HttpStatusCode.InternalServerError)
                call.respondText { "Unable to merge PDF documents ${e.message}" }
            }
        }

        post("/mergeWithSeparator") {
            try {

                val documents: MutableMap<String, ByteArray> = mutableMapOf()
                val info: MutableList<String> = mutableListOf()

                val multipartData = call.receiveMultipart()
                        multipartData.forEachPart { part ->
                            when (part) {
                                is  PartData.FileItem -> {
                                    val filename = part.originalFileName as String
                                    documents.put(filename, part.streamProvider().readBytes())
                                }
                                is PartData.FormItem -> {
                                    info.add(part.value)
                                }
                                is PartData.BinaryItem -> TODO()
                            }
                        }
                //TODO: Legg på validering
                // antall filer stemmer med json
                val mapper = jacksonObjectMapper()
                val mergeInfo: MergeInfo = mapper.readValue(info.get(0), MergeInfo::class.java)

                val mergedDocument = pdfMerger.mergeWithSeparator(mergeInfo, documents)

                call.respondBytes(bytes = mergedDocument, contentType = Pdf)
            } catch (e: Exception) {
                logger.error("Unable to merge pdf documents", e)
                call.response.status(HttpStatusCode.InternalServerError)
                call.respondText { "Unable to merge PDF documents ${e.message}" }
            }
        }
    }
}

private fun correlationId(call: ApplicationCall) = listOfNotNull(
    call.request.headers["Nav-Call-Id"],
    call.request.headers["x-correlation-id"]
).firstOrNull()

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::main
    )
        .start(wait = true)
}
