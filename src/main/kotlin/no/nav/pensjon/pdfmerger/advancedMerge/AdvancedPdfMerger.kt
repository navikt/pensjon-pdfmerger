package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import no.nav.pensjon.pdfmerger.Dokumentinfo
import no.nav.pensjon.pdfmerger.MergeInfo
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class AdvancedPdfMerger(
    val mergeinfo: MergeInfo
) {
    private val HEADING_FONT: Font = Font(Font.TIMES_ROMAN, 24f, Font.BOLD)
    private val INFO_FONT_BOLD: Font = Font(Font.TIMES_ROMAN, 18f, Font.BOLD)
    private val NORMAL_FONT: Font = Font(Font.TIMES_ROMAN, 11f, Font.NORMAL)
    private val SPACING = 50
    private val PAGE_MARGIN = 10.0f
    private val EMPTY_PARAGRAPH = Paragraph(" ")
    private val dateFormat: String = "dd.MM.yyyy"

    private val document: Document
    private val byteArrayOutputStream: ByteArrayOutputStream
    private val pdfWriter: PdfWriter

    init {
        EMPTY_PARAGRAPH.setSpacingAfter(SPACING.toFloat())
        document = Document()
        byteArrayOutputStream = ByteArrayOutputStream()
        pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream)
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
        createFrontPage()

        // Append documents with separator pages
        var i = 1
        for (documentinfo in mergeinfo.dokumentinfo) {
            createSeparatorPage(i++, mergeinfo.dokumentinfo.size, documentinfo)
            appendDocumentWithVedlegg(documentinfo)
        }
        if (document.isOpen) {
            document.close()
        }
    }

    private fun createFrontPage() {
        document.add(EMPTY_PARAGRAPH)

        val title = Paragraph(
            "Dokumentoversikt for",
            HEADING_FONT
        )
        title.setAlignment(Paragraph.ALIGN_CENTER)
        title.setSpacingAfter(SPACING.toFloat())
        document.add(title)

        val gjelder = Paragraph()
        gjelder.add(Chunk("${mergeinfo.gjelderID} ${mergeinfo.gjelderNavn}", INFO_FONT_BOLD))
        gjelder.setAlignment(Paragraph.ALIGN_CENTER)
        gjelder.setSpacingAfter(SPACING.toFloat())
        document.add(gjelder)

        document.add(createContentsTable())
        document.newPage()
    }

    private fun createContentsTable(): PdfPTable {

        val contentsTable = PdfPTable(8)
        val columnWidths = intArrayOf(3, 3, 4, 5, 4, 5, 5, 5)

        contentsTable.apply {
            widthPercentage = 90f
            setWidths(columnWidths)
            addCell(Phrase("Nr", NORMAL_FONT))
            addCell(Phrase("Dok inn/Ut/Notat", NORMAL_FONT))
            addCell(Phrase("Reg.dato (I) Ferdigstilt dato (U/N)", NORMAL_FONT))
            addCell(Phrase("Tema", NORMAL_FONT))
            addCell(Phrase("Sak", NORMAL_FONT))
            addCell(Phrase("Avsender (I) Mottaker (U)", NORMAL_FONT))
            addCell(Phrase("Innhold", NORMAL_FONT))
            addCell(Phrase("Vedlegg", NORMAL_FONT))
            headerRows = 1
        }

        var documentNr = 1
        for (documentinfo in mergeinfo.dokumentinfo) {
            val cell = PdfPCell(Phrase(documentinfo.dokumenttype, NORMAL_FONT))
            cell.horizontalAlignment = Element.ALIGN_CENTER

            contentsTable.apply {
                addCell(
                    Phrase(
                        "" + documentNr++ + " av " + mergeinfo.dokumentinfo.size, NORMAL_FONT
                    )
                )
                addCell(cell)
                addCell(
                    Phrase(
                        documentinfo.mottattSendtDato.format(
                            DateTimeFormatter.ofPattern(dateFormat)
                        ),
                        NORMAL_FONT
                    )
                )
                addCell(Phrase(documentinfo.fagomrade, NORMAL_FONT))
                addCell(Phrase(documentinfo.saknr, NORMAL_FONT))
                addCell(Phrase(documentinfo.avsenderMottaker, NORMAL_FONT))
                addCell(Phrase(documentinfo.dokumentnavn, NORMAL_FONT))
                addCell(Phrase(" ", NORMAL_FONT))
            }

            documentinfo.vedleggListe.forEach { vedlegg ->
                addEmptyCells(contentsTable, 7)
                contentsTable.addCell(Phrase(vedlegg.dokumentnavn, NORMAL_FONT))
            }
        }
        return contentsTable
    }

    private fun appendDocumentWithVedlegg(documentinfo: Dokumentinfo) {
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

    private fun createSeparatorPage(docNr: Int, totalDocs: Int, dokList: Dokumentinfo) {
        document.add(EMPTY_PARAGRAPH)
        val heading = Paragraph(mergeinfo.gjelderNavn, INFO_FONT_BOLD)
        heading.setSpacingAfter(SPACING.toFloat())
        heading.setAlignment(Paragraph.ALIGN_CENTER)
        document.add(heading)
        document.add(createDocInfoLine(docNr, totalDocs, dokList))
        addVedlegginfoToDocument(dokList)
        document.newPage()
    }

    private fun createCenterInfoParagraph(paragraphString: String): Paragraph {
        val paragraph = Paragraph(paragraphString, INFO_FONT_BOLD)
        paragraph.apply {
            setAlignment(Paragraph.ALIGN_CENTER)
            setIndentationLeft(PAGE_MARGIN)
            setIndentationRight(PAGE_MARGIN)
        }
        return paragraph
    }

    private fun addEmptyCells(contentsTable: PdfPTable, antallTomCell: Int) {
        var index = 0
        while (index < antallTomCell) {
            contentsTable.addCell(Phrase(" ", NORMAL_FONT))
            index++
        }
    }

    private fun createDocInfoLine(docNum: Int, totalDocs: Int, docinfo: Dokumentinfo): Paragraph {
        val innhold = if (docinfo.dokumentnavn.isEmpty()) ": " + docinfo.dokumentnavn else ""
        return createCenterInfoParagraph(
            "Dokument nr " + docNum + " av " + totalDocs + innhold
        )
    }

    private fun addVedlegginfoToDocument(documentinfo: Dokumentinfo) {
        if (!documentinfo.vedleggListe.isEmpty()) {
            val spacer = Paragraph(" ")
            spacer.setSpacingAfter((SPACING / 2).toFloat())
            document.add(spacer)
            val paragraph = createCenterInfoParagraph("Vedlegg" + ": ")
            document.add(paragraph)
            for (vedlegg in documentinfo.vedleggListe) {
                document.add(createCenterInfoParagraph(vedlegg.dokumentnavn))
            }
        }
    }
}
