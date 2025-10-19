package no.nav.pensjon.pdfmerger

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.BaseUnits
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.pensjon.pdfmerger.advancedMerge.AdvancedPdfMerger
import no.nav.pensjon.pdfmerger.advancedMerge.MergeRequest
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.ByteArrayOutputStream

class MeteringPdfMerger : MeterBinder {
    private lateinit var mergeCallCount: Counter
    private lateinit var documentCount: DistributionSummary
    private lateinit var documentSize: DistributionSummary
    private lateinit var mergedDocumentSize: DistributionSummary
    private lateinit var mergeTimer: Timer

    private lateinit var mergeWithSeparatorCallCount: Counter
    private lateinit var mergeWithSeparatorDocumentCount: DistributionSummary
    private lateinit var mergeWithSeparatorDocumentSize: DistributionSummary
    private lateinit var mergeWithSeparatorMergedDocumentSize: DistributionSummary
    private lateinit var mergeWithSeparatorTimer: Timer

    fun mergeDocuments(documents: List<ByteArray>): ByteArray {
        mergeCallCount.increment()

        return mergeTimer.recordCallable {
            val outputStream = ByteArrayOutputStream()

            val pdfMerger = PDFMergerUtility().apply {
                destinationStream = outputStream
            }

            documentCount.record(documents.size.toDouble())
            documents.forEach {
                documentSize.record(it.size.toDouble())
                pdfMerger.addSource(RandomAccessReadBuffer(it))
            }

            pdfMerger.mergeDocuments(null)
            val mergedDocument = outputStream.toByteArray()

            mergedDocumentSize.record(mergedDocument.size.toDouble())

            mergedDocument
        }!!
    }

    fun mergeWithSeparator(
        mergeinfo: MergeInfo,
        documents: Map<String, ByteArray>
    ): ByteArray {
        mergeWithSeparatorCallCount.increment()

        return mergeWithSeparatorTimer.recordCallable {
            recordDocumentsToMerge(documents)

            val mergedDocument = AdvancedPdfMerger().merge(MergeRequest(mergeinfo, documents))
            mergeWithSeparatorMergedDocumentSize.record(mergedDocument.size.toDouble())

            mergedDocument
        }!!
    }

    private fun recordDocumentsToMerge(documents: Map<String, ByteArray>) {
        mergeWithSeparatorDocumentCount.record(documents.size.toDouble())
        documents.forEach {
            mergeWithSeparatorDocumentSize.record(it.value.size.toDouble())
        }
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

        mergeCallCount = Counter.builder("merger.call.count")
            .baseUnit("calls")
            .register(meterRegistry)

        mergeTimer = Timer.builder("merger.merge.timer")
            .description("Time of merge operation")
            .register(meterRegistry)

        mergeWithSeparatorDocumentCount = DistributionSummary
            .builder("mergeWithSeparator.document.count")
            .description("Number of documents to merge with mergeWithSeparator per call")
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .baseUnit(BaseUnits.FILES)
            .register(meterRegistry)

        mergeWithSeparatorDocumentSize = DistributionSummary
            .builder("mergeWithSeparator.document.size")
            .baseUnit(BaseUnits.BYTES)
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .description("Size of documents to merge with mergeWithSeparator")
            .register(meterRegistry)

        mergeWithSeparatorMergedDocumentSize = DistributionSummary
            .builder("mergeWithSeparator.merged.document.size")
            .baseUnit(BaseUnits.BYTES)
            .publishPercentiles(0.1, 0.25, 0.5, 0.75, 0.9)
            .description("Size of merged document through mergeWithSeparator")
            .register(meterRegistry)

        mergeWithSeparatorCallCount = Counter.builder("mergeWithSeparator.call.count")
            .baseUnit("calls")
            .register(meterRegistry)

        mergeWithSeparatorTimer = Timer.builder("merger.mergeWithSeparator.timer")
            .description("Time of mergeWithSeparator operation")
            .register(meterRegistry)
    }
}
