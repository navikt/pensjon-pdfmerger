package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import no.nav.pensjon.pdfmerger.MergeInfo
import java.io.ByteArrayOutputStream

class AdvancedPdfMerger(
    val mergeinfo: MergeInfo
) {

    private val document: Document
    private val byteArrayOutputStream: ByteArrayOutputStream
    private val pdfWriter: PdfWriter
    private val frontpageGenerator: FrontpageGenerator
    private val separatorpageGenerator: SeparatorpageGenerator
    private val appendDocuments: AppendDocuments

    init {
        document = Document()
        byteArrayOutputStream = ByteArrayOutputStream()
        pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream)
        frontpageGenerator = FrontpageGenerator()
        separatorpageGenerator = SeparatorpageGenerator(document)
        appendDocuments = AppendDocuments(document, pdfWriter)
    }

    fun generatePdfResponse(): ByteArray {
        createDocument()
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Generates the new PDF document consisting of
     * one front page with a table of content
     * for each document
     *  a separatorpage
     *  the page
     *  its vedlegg
     */
    private fun createDocument() {
        document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN)
        document.open()

        frontpageGenerator.createFrontPage(
            document,
            mergeinfo.gjelderID,
            mergeinfo.gjelderNavn,
            mergeinfo.dokumentinfo
        )

        var i = 1
        for (documentinfo in mergeinfo.dokumentinfo) {
            separatorpageGenerator.createSeparatorPage(
                i++,
                mergeinfo.dokumentinfo.size,
                documentinfo,
                mergeinfo.gjelderNavn
            )
            appendDocuments.appendDocumentWithVedlegg(documentinfo)
        }

        if (document.isOpen) {
            document.close()
        }
    }
}
