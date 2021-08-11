package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import no.nav.pensjon.pdfmerger.advancedMerge.models.VedleggDokument

class SeparatorpageGenerator {
    fun createSeparatorPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        dokumentNr: Int,
        dokumentinfo: Dokumentinfo,
    ) {
        mergeContext.document.add(mergeContext.emptyParagraph)
        val heading = Paragraph(mergeRequest.gjelderNavn, INFO_FONT_BOLD)
        heading.setSpacingAfter(SPACING.toFloat())
        heading.setAlignment(Paragraph.ALIGN_CENTER)
        mergeContext.document.add(heading)
        mergeContext.document.add(createDokInfoLine(dokumentNr, mergeRequest.totalDocs, dokumentinfo))
        addVedlegginfoToDocument(mergeContext.document, dokumentinfo.vedleggListe)
        mergeContext.document.newPage()
    }

    private fun createDokInfoLine(dokumentNr: Int, totalDokumentinfo: Int, dokumentinfo: Dokumentinfo): Paragraph {
        val innhold = if (dokumentinfo.dokumentnavn == null) "" else ": " + dokumentinfo.dokumentnavn
        return createCenterInfoParagraph(
            "Dokument nr $dokumentNr av $totalDokumentinfo$innhold"
        )
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

    private fun addVedlegginfoToDocument(document: Document, vedleggListe: List<VedleggDokument>) {
        if (vedleggListe.isNotEmpty() == true) {
            val spacer = Paragraph(" ")
            spacer.setSpacingAfter((SPACING / 2).toFloat())
            document.add(spacer)
            val paragraph = createCenterInfoParagraph("Vedlegg: ")
            document.add(paragraph)
            vedleggListe.forEach {
                document.add(createCenterInfoParagraph(it.dokumentnavn))
            }
        }
    }
}
