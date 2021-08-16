package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.SPACING
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokument
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import java.time.format.DateTimeFormatter

class FrontpageGenerator {
    private val HEADING_FONT: Font = Font(Font.TIMES_ROMAN, 24f, Font.BOLD)
    private val NORMAL_FONT: Font = Font(Font.TIMES_ROMAN, 11f, Font.NORMAL)

    fun createFrontPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest
    ) {
        mergeContext.document.add(MergeContext.EMPTY_PARAGRAPH)

        val title = Paragraph(
            "Dokumentoversikt for",
            HEADING_FONT
        )
        title.setAlignment(Paragraph.ALIGN_CENTER)
        title.setSpacingAfter(SPACING.toFloat())
        mergeContext.document.add(title)

        val gjelder = Paragraph()
        gjelder.add(Chunk("${mergeRequest.gjelderID} ${mergeRequest.gjelderNavn}", MergeContext.INFO_FONT_BOLD))
        gjelder.setAlignment(Paragraph.ALIGN_CENTER)
        gjelder.setSpacingAfter(SPACING.toFloat())
        mergeContext.document.add(gjelder)

        mergeContext.document.add(createContentsTable(mergeRequest.dokumentinfo))
    }

    private fun createContentsTable(dokumentinfoListe: List<Dokumentinfo>): PdfPTable {

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

        dokumentinfoListe.forEachIndexed { index, dokumentinfo ->
            val cell = PdfPCell(Phrase(dokumentinfo.dokumenttype, NORMAL_FONT))
            cell.horizontalAlignment = Element.ALIGN_CENTER

            val firstDokument = dokumentinfo.getSortedDokumenter().get(0)
            val otherDokuments: List<Dokument>
            if (dokumentinfo.getSortedDokumenter().size > 1) {
                otherDokuments = dokumentinfo.getSortedDokumenter().subList(1, dokumentinfo.getSortedDokumenter().size)
            } else {
                otherDokuments = listOf()
            }

            contentsTable.apply {
                addCell(
                    Phrase(
                        "" + (index + 1) + " av " + dokumentinfoListe.size, NORMAL_FONT
                    )
                )
                addCell(cell)
                addCell(
                    Phrase(
                        dokumentinfo.mottattSendtDato.format(
                            DateTimeFormatter.ofPattern(dateFormat)
                        ),
                        NORMAL_FONT
                    )
                )
                addCell(Phrase(dokumentinfo.fagomrade, NORMAL_FONT))
                addCell(Phrase(dokumentinfo.saknr, NORMAL_FONT))
                addCell(Phrase(dokumentinfo.avsenderMottaker, NORMAL_FONT))
                if (dokumentinfo.hoveddokument != null) {
                    addCell(Phrase(firstDokument.dokumentnavn, NORMAL_FONT))
                    addCell(Phrase(" ", NORMAL_FONT))
                } else {
                    addCell(Phrase(" ", NORMAL_FONT))
                    addCell(Phrase(firstDokument.dokumentnavn, NORMAL_FONT))
                }
            }

            otherDokuments.forEach { vedlegg ->
                addEmptyCells(contentsTable)
                contentsTable.addCell(Phrase(vedlegg.dokumentnavn, NORMAL_FONT))
            }
        }
        return contentsTable
    }

    private fun addEmptyCells(contentsTable: PdfPTable) {
        var index = 0
        while (index < 7) {
            contentsTable.addCell(Phrase(" ", NORMAL_FONT))
            index++
        }
    }
}
