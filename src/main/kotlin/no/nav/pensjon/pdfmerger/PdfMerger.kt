package no.nav.pensjon.pdfmerger

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.BaseUnits
import io.micrometer.core.instrument.binder.MeterBinder
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.ByteArrayOutputStream

class PdfMerger : MeterBinder {
    private lateinit var callCount: Counter
    private lateinit var documentCount: DistributionSummary
    private lateinit var documentSize: DistributionSummary
    private lateinit var mergedDocumentSize: DistributionSummary

    private lateinit var mergeTimer: Timer

    fun mergeDocuments(documents: List<ByteArray>): ByteArray {
        callCount.increment()

        return mergeTimer.recordCallable {
            val outputStream = ByteArrayOutputStream()

            val pdfMerger = PDFMergerUtility().apply {
                destinationStream = outputStream
            }

            documentCount.record(documents.size.toDouble())
            documents.forEach {
                documentSize.record(it.size.toDouble())
                pdfMerger.addSource(it.inputStream())
            }

            pdfMerger.mergeDocuments(null)
            val mergedDocument = outputStream.toByteArray()

            mergedDocumentSize.record(mergedDocument.size.toDouble())

            mergedDocument
        }!!
    }

    override fun bindTo(meterRegistry: MeterRegistry) {
        documentCount = DistributionSummary.builder("merger.document.count")
            .description("Number of documents to merge per call")
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .baseUnit(BaseUnits.FILES)
            .register(meterRegistry)

        documentSize = DistributionSummary.builder("merger.document.size")
            .baseUnit(BaseUnits.BYTES)
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .description("Size of documents to merge")
            .register(meterRegistry)

        mergedDocumentSize = DistributionSummary.builder("merger.merged.document.size")
            .baseUnit(BaseUnits.BYTES)
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .description("Size of merged document")
            .register(meterRegistry)

        callCount = Counter.builder("merger.call.count")
            .baseUnit("calls")
            .register(meterRegistry)

        mergeTimer = Timer.builder("merger.merge.timer")
            .description("Time of merge operation")
            .register(meterRegistry)
    }
}
