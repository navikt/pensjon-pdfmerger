package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import no.nav.pensjon.pdfmerger.Dokumentinfo
import no.nav.pensjon.pdfmerger.MergeInfo
import java.io.ByteArrayOutputStream

val EMPTY_PARAGRAPH = Paragraph(" ")
val PAGE_MARGIN = 10.0f

class AdvancedPdfMerger(
    val mergeinfo: MergeInfo
) {

    private val document: Document
    private val byteArrayOutputStream: ByteArrayOutputStream
    private val pdfWriter: PdfWriter
    private val separatorpageGenerator: SeparatorpageGenerator

    init {
        document = Document()
        byteArrayOutputStream = ByteArrayOutputStream()
        pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream)
        separatorpageGenerator = SeparatorpageGenerator(document)
        EMPTY_PARAGRAPH.setSpacingAfter(SPACING.toFloat())
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

        createFrontPage(
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
            appendDocumentWithVedlegg(documentinfo)
        }

        if (document.isOpen) {
            document.close()
        }
    }

    fun appendDocumentWithVedlegg(documentinfo: Dokumentinfo) {
        val files: MutableList<ByteArray> = ArrayList()

        files.add(documentinfo.fil)
        documentinfo.vedleggListe.forEach { files.add(it.fil) }

        for (file in files) {
            document.setMargins(0f, 0f, -14f, 0f)
            val reader = PdfReader(file)
            var page: PdfImportedPage
            for (p in 1..reader.numberOfPages) {
                page = pdfWriter.getImportedPage(reader, p)
                val image = Image.getInstance(page)
                image.scaleToFit(PageSize.A4.width(), PageSize.A4.height())
                document.add(image)
                if (p < reader.numberOfPages) {
                    document.newPage()
                }
            }
            document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN)
            document.newPage()
            pdfWriter.freeReader(reader)
            reader.close()
        }
    }
}
