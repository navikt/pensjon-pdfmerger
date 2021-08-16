package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokument
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo

class SeparatorpageGenerator(val PAGE_MARGIN: Float) {

    fun createSeparatorPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        dokumentNr: Int,
        dokumentinfo: Dokumentinfo,
    ) {
        mergeContext.document.newPage()
        mergeContext.document.add(mergeContext.emptyParagraph)

        val heading = Paragraph(mergeRequest.gjelderNavn, mergeContext.INFO_FONT_BOLD)
        heading.setAlignment(Paragraph.ALIGN_CENTER)
        heading.setSpacingAfter(mergeContext.SPACING.toFloat())
        mergeContext.document.add(heading)

        mergeContext.document.add(
            createDokInfoLine(
                dokumentNr,
                mergeRequest.totalDokumentinfo,
                dokumentinfo,
                mergeContext.INFO_FONT_BOLD
            )
        )

        addVedlegginfoToDocument(
            mergeContext.document,
            dokumentinfo.vedleggListe,
            mergeContext.SPACING,
            mergeContext.INFO_FONT_BOLD
        )
    }

    private fun createDokInfoLine(
        dokumentNr: Int,
        totalDokumentinfo: Int,
        dokumentinfo: Dokumentinfo,
        infoFontBold: Font
    ): Paragraph {
        val innhold = if (dokumentinfo.hoveddokument == null) "" else ": " + dokumentinfo.hoveddokument.dokumentnavn
        return createCenterInfoParagraph(
            "Dokument nr $dokumentNr av $totalDokumentinfo$innhold",
            infoFontBold
        )
    }

    private fun createCenterInfoParagraph(paragraphString: String, infoFontBold: Font): Paragraph {
        val paragraph = Paragraph(paragraphString, infoFontBold)
        paragraph.apply {
            setAlignment(Paragraph.ALIGN_CENTER)
            setIndentationLeft(PAGE_MARGIN)
            setIndentationRight(PAGE_MARGIN)
        }
        return paragraph
    }

    private fun addVedlegginfoToDocument(
        document: Document,
        vedleggListe: List<Dokument>,
        spacing: Int,
        infoFontBold: Font
    ) {
        if (vedleggListe.isNotEmpty()) {
            val spacer = Paragraph(" ")
            spacer.setSpacingAfter((spacing / 2).toFloat())
            document.add(spacer)

            val paragraph = createCenterInfoParagraph("Vedlegg: ", infoFontBold)
            document.add(paragraph)
            vedleggListe.forEach {
                document.add(createCenterInfoParagraph(it.dokumentnavn, infoFontBold))
            }
        }
    }
}
