package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import no.nav.pensjon.pdfmerger.Dokumentinfo
import java.time.format.DateTimeFormatter

val EMPTY_PARAGRAPH = Paragraph(" ")
val HEADING_FONT: Font = Font(Font.TIMES_ROMAN, 24f, Font.BOLD)
val SPACING = 50
val INFO_FONT_BOLD: Font = Font(Font.TIMES_ROMAN, 18f, Font.BOLD)
val NORMAL_FONT: Font = Font(Font.TIMES_ROMAN, 11f, Font.NORMAL)

class FrontpageGenerator {

    init {
        EMPTY_PARAGRAPH.setSpacingAfter(SPACING.toFloat())
    }

    fun createFrontPage(
        document: Document,
        gjelderID: String,
        gjelderNavn: String,
        dokumentinfo: List<Dokumentinfo>
    ) {
        document.add(EMPTY_PARAGRAPH)

        val title = Paragraph(
            "Dokumentoversikt for",
            HEADING_FONT
        )
        title.setAlignment(Paragraph.ALIGN_CENTER)
        title.setSpacingAfter(SPACING.toFloat())
        document.add(title)

        val gjelder = Paragraph()
        gjelder.add(Chunk("$gjelderID $gjelderNavn", INFO_FONT_BOLD))
        gjelder.setAlignment(Paragraph.ALIGN_CENTER)
        gjelder.setSpacingAfter(SPACING.toFloat())
        document.add(gjelder)

        document.add(createContentsTable(dokumentinfo))
        document.newPage()
    }

    private fun createContentsTable(dokumentinfo: List<Dokumentinfo>): PdfPTable {

        val contentsTable = PdfPTable(8)
        val columnWidths = intArrayOf(3, 3, 4, 5, 4, 5, 5, 5)
        val dateFormat = "dd.MM.yyyy"

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
        for (documentinfo in dokumentinfo) {
            val cell = PdfPCell(Phrase(documentinfo.dokumenttype, NORMAL_FONT))
            cell.horizontalAlignment = Element.ALIGN_CENTER

            contentsTable.apply {
                addCell(
                    Phrase(
                        "" + documentNr++ + " av " + dokumentinfo.size, NORMAL_FONT
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

    private fun addEmptyCells(contentsTable: PdfPTable, antallTomCell: Int) {
        var index = 0
        while (index < antallTomCell) {
            contentsTable.addCell(Phrase(" ", NORMAL_FONT))
            index++
        }
    }
}
