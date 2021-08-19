package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Image.getInstance
import com.lowagie.text.PageSize.A4
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfWriter
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.PAGE_MARGIN
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import java.io.Closeable

class AdvancedPdfMerger(
    private val frontpageGenerator: FrontpageGenerator = FrontpageGenerator(),
    private val separatorpageGenerator: SeparatorpageGenerator = SeparatorpageGenerator()
) {
    /**
     * Generates the new PDF document consisting of
     * one front page with a table of content
     * for each document
     *  a separatorpage
     *  the page
     *  its vedlegg
     */
    fun merge(mergeRequest: MergeRequest): ByteArray = MergeContext().use { mergeContext ->
        frontpageGenerator.createFrontPage(mergeContext, mergeRequest)

        mergeRequest.dokumentinfo.forEachIndexed { i, dokumentinfo ->
            separatorpageGenerator.createSeparatorPage(
                mergeContext,
                mergeRequest,
                i + 1,
                dokumentinfo,
            )

            appendDocumentWithAttachments(mergeContext, mergeRequest, dokumentinfo)
        }

        mergeContext
    }.byteArrayOutputStream.toByteArray()

    // If no hoveddokument is given we still need to add its vedlegg
    private fun appendDocumentWithAttachments(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        dokumentinfo: Dokumentinfo
    ) {
        mergeRequest.findFiles(dokumentinfo).forEach { file ->
            mergeContext.document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN)

            ClosablePdfReader(mergeContext.pdfWriter, file).use { reader ->
                for (pageNumber in 1..reader.numberOfPages) {
                    mergeContext.document.newPage()

                    mergeContext.document.add(
                        getInstance(
                            mergeContext.pdfWriter.getImportedPage(
                                reader,
                                pageNumber
                            )
                        ).apply {
                            scaleToFit(A4.width(), A4.height())
                        }
                    )
                }
            }
        }
    }

    class ClosablePdfReader(val pdfWriter: PdfWriter, file: ByteArray) : PdfReader(file), Closeable {
        override fun close() {
            pdfWriter.freeReader(this)
            super.close()
        }
    }
}
