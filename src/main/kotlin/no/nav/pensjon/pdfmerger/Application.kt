package no.nav.pensjon.pdfmerger

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
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
        mdc("application-id") { call: ApplicationCall ->
            call.request.header("x-application-id")
        }

        mdc("correlation-id", ::correlationId)
        mdc("transaction", ::correlationId)

        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }

        format { call ->
            "\"${call.request.httpMethod.value} ${call.request.uri} ${call.request.httpVersion}\" ${call.response.status()?.value}"
        }
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
    call.request.header("Nav-Call-Id"),
    call.request.header("x-correlation-id")
).firstOrNull()

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::main
    )
        .start(wait = true)
}
