package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.EMPTY_PARAGRAPH
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.INFO_FONT_BOLD
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.PAGE_MARGIN
import no.nav.pensjon.pdfmerger.advancedMerge.MergeContext.Companion.SPACING
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokument
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo

class SeparatorpageGenerator {
    fun createSeparatorPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        dokumentNr: Int,
        dokumentinfo: Dokumentinfo,
    ) {
        mergeContext.document.newPage()
        mergeContext.document.add(EMPTY_PARAGRAPH)

        mergeContext.document.add(
            Paragraph(mergeRequest.gjelderNavn, INFO_FONT_BOLD).apply {
                setAlignment(Paragraph.ALIGN_CENTER)
                setSpacingAfter(SPACING)
            }
        )

        mergeContext.document.add(
            createDokInfoLine(
                dokumentNr,
                mergeRequest.totalDokumentinfo,
                dokumentinfo
            )
        )

        addVedlegginfoToDocument(
            mergeContext.document,
            dokumentinfo.vedleggListe
        )
    }

    private fun createDokInfoLine(
        dokumentNr: Int,
        totalDokumentinfo: Int,
        dokumentinfo: Dokumentinfo
    ): Paragraph {
        val innhold = if (dokumentinfo.hoveddokument == null) "" else ": " + dokumentinfo.hoveddokument.dokumentnavn
        return createCenterInfoParagraph(
            "Dokument nr $dokumentNr av $totalDokumentinfo$innhold"
        )
    }

    private fun createCenterInfoParagraph(paragraphString: String) = Paragraph(paragraphString, INFO_FONT_BOLD).apply {
        setAlignment(Paragraph.ALIGN_CENTER)
        setIndentationLeft(PAGE_MARGIN)
        setIndentationRight(PAGE_MARGIN)
    }

    private fun addVedlegginfoToDocument(
        document: Document,
        vedleggListe: List<Dokument>
    ) {
        if (vedleggListe.isNotEmpty()) {
            document.add(
                Paragraph(" ").apply {
                    setSpacingAfter((SPACING / 2))
                }
            )

            document.add(createCenterInfoParagraph("Vedlegg: "))

            vedleggListe.forEach {
                document.add(createCenterInfoParagraph(it.dokumentnavn))
            }
        }
    }
}
