package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.PAGE_MARGIN
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo

class AdvancedPdfMerger(
    private val frontpageGenerator: FrontpageGenerator = FrontpageGenerator(),
    private val separatorpageGenerator: SeparatorpageGenerator = SeparatorpageGenerator()
) {
    fun merge(mergeRequest: MergeRequest): ByteArray {
        val mergeContext = createDocument(mergeRequest)

        return mergeContext.byteArrayOutputStream.toByteArray()
    }

    /**
     * Generates the new PDF document consisting of
     * one front page with a table of content
     * for each document
     *  a separatorpage
     *  the page
     *  its vedlegg
     */
    private fun createDocument(mergeRequest: MergeRequest): MergeContext {
        val mergeContext = MergeContext()
        mergeContext.document.open()

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

        if (mergeContext.document.isOpen) {
            mergeContext.document.close()
        }

        return mergeContext
    }

    // If no hoveddokument is given we still need to add its vedlegg
    private fun appendDocumentWithAttachments(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        documentinfo: Dokumentinfo
    ) {
        mergeRequest.findFiles(documentinfo).forEach { file ->
            mergeContext.document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN)
            mergeContext.document.newPage()
            val reader = PdfReader(file)
            var page: PdfImportedPage
            for (p in 1..reader.numberOfPages) {
                page = mergeContext.pdfWriter.getImportedPage(reader, p)
                val image = Image.getInstance(page)
                image.scaleToFit(PageSize.A4.width(), PageSize.A4.height())
                mergeContext.document.add(image)
                if (p < reader.numberOfPages) {
                    mergeContext.document.newPage()
                }
            }
            mergeContext.pdfWriter.freeReader(reader)
            reader.close()
        }
    }
}
