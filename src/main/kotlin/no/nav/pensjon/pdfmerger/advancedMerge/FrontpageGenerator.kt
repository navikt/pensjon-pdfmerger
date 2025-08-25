package no.nav.pensjon.pdfmerger.advancedMerge

import org.openpdf.text.Chunk
import org.openpdf.text.Font
import org.openpdf.text.Paragraph
import org.openpdf.text.Paragraph.ALIGN_CENTER
import org.openpdf.text.Phrase
import org.openpdf.text.pdf.PdfPCell
import org.openpdf.text.pdf.PdfPTable
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.EMPTY_PARAGRAPH
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.INFO_FONT_BOLD
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.SPACING
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import java.time.format.DateTimeFormatter.ofPattern

class FrontpageGenerator {
    fun createFrontPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest
    ) {
        mergeContext.document.add(EMPTY_PARAGRAPH)

        mergeContext.document.add(
            Paragraph("Dokumentoversikt for", HEADING_FONT).apply {
                setAlignment(ALIGN_CENTER)
                setSpacingAfter(SPACING)
            }
        )

        mergeContext.document.add(
            Paragraph().apply {
                add(Chunk("${mergeRequest.gjelderID} ${mergeRequest.gjelderNavn}", INFO_FONT_BOLD))
                setAlignment(ALIGN_CENTER)
                setSpacingAfter(SPACING)
            }
        )

        mergeContext.document.add(contentsTable(mergeRequest.dokumentinfo))
    }

    private fun contentsTable(dokumentinfoListe: List<Dokumentinfo>) = PdfPTable(8).apply {
        headerRows = 1
        widthPercentage = 90f
        setWidths(intArrayOf(3, 3, 4, 5, 4, 5, 5, 5))

        addPhrase("Nr.")
        addPhrase("Dok. type")
        addPhrase("Mottatt/sendt")
        addPhrase("Tema")
        addPhrase("Saksnummer")
        addPhrase("Avsender")
        addPhrase("Hoveddokument")
        addPhrase("Vedlegg")

        dokumentinfoListe.forEachIndexed { index, dokumentinfo ->
            addPhrase("${index + 1} av ${dokumentinfoListe.size}")
            addPhrase(dokumentinfo.dokumenttype, horizontalAlignment = ALIGN_CENTER)
            addPhrase(dokumentinfo.mottattSendtDato.format(ofPattern("dd.MM.yyyy")))
            addPhrase(dokumentinfo.fagomrade)
            addPhrase(dokumentinfo.saknr)
            addPhrase(dokumentinfo.avsenderMottaker)

            if (dokumentinfo.hasHovedDokument) {
                addPhrase(dokumentinfo.dokumentnavn)
                addPhrase(null)
            } else {
                addPhrase(null)
                addPhrase(dokumentinfo.dokumentnavn)
            }

            dokumentinfo.otherDokuments.forEach { vedlegg ->
                // 7 empty columns
                for (i in 0 until 7) addPhrase(null)

                addPhrase(vedlegg.dokumentnavn)
            }
        }
    }

    private fun PdfPTable.addPhrase(string: String?, font: Font = NORMAL_FONT) = addCell(Phrase(string, font))
    private fun PdfPTable.addPhrase(string: String?, font: Font = NORMAL_FONT, horizontalAlignment: Int) =
        addCell(PdfPCell(Phrase(string, font)).apply { this.horizontalAlignment = horizontalAlignment })

    companion object {
        private val HEADING_FONT: Font = Font(Font.TIMES_ROMAN, 24f, Font.BOLD)
        private val NORMAL_FONT: Font = Font(Font.TIMES_ROMAN, 11f, Font.NORMAL)
    }
}
