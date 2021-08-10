package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import java.time.format.DateTimeFormatter

val SPACING = 50
val INFO_FONT_BOLD: Font = Font(Font.TIMES_ROMAN, 18f, Font.BOLD)
private val HEADING_FONT: Font = Font(Font.TIMES_ROMAN, 24f, Font.BOLD)
private val NORMAL_FONT: Font = Font(Font.TIMES_ROMAN, 11f, Font.NORMAL)

class FrontpageGenerator {
    fun createFrontPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest
    ) {
        mergeContext.document.add(mergeContext.emptyParagraph)

        val title = Paragraph(
            "Dokumentoversikt for",
            HEADING_FONT
        )
        title.setAlignment(Paragraph.ALIGN_CENTER)
        title.setSpacingAfter(SPACING.toFloat())
        mergeContext.document.add(title)

        val gjelder = Paragraph()
        gjelder.add(Chunk("${mergeRequest.gjelderID} ${mergeRequest.gjelderNavn}", INFO_FONT_BOLD))
        gjelder.setAlignment(Paragraph.ALIGN_CENTER)
        gjelder.setSpacingAfter(SPACING.toFloat())
        mergeContext.document.add(gjelder)

        mergeContext.document.add(createContentsTable(mergeRequest.dokumentinfo))
        mergeContext.document.newPage()
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

            documentinfo.vedleggListe?.forEach { vedlegg ->
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
