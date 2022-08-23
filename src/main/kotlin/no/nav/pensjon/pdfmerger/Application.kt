package no.nav.pensjon.pdfmerger

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
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
import no.nav.pensjon.pdfmerger.routes.simpleMerge
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.event.Level

val logger: Logger = getLogger(Application::class.java)

fun Application.main() {
    val meteringPdfMerger = MeteringPdfMerger()
    val mapper = jsonMapper {
        addModule(kotlinModule())
        addModule(JavaTimeModule())
    }

    val appMicrometerRegistry = PrometheusMeterRegistry(DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry

        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            meteringPdfMerger
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

        simpleMerge(meteringPdfMerger)

        mergeWithSeparator(meteringPdfMerger, mapper)
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
