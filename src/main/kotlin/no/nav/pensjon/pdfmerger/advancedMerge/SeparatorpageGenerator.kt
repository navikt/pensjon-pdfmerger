package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo

class SeparatorpageGenerator {
    fun createSeparatorPage(
        mergeContext: MergeContext,
        mergeRequest: MergeRequest,
        docNr: Int,
        dokList: Dokumentinfo,
    ) {
        mergeContext.document.add(mergeContext.emptyParagraph)
        val heading = Paragraph(mergeRequest.gjelderNavn, INFO_FONT_BOLD)
        heading.setSpacingAfter(SPACING.toFloat())
        heading.setAlignment(Paragraph.ALIGN_CENTER)
        mergeContext.document.add(heading)
        mergeContext.document.add(createDocInfoLine(docNr, mergeRequest.totalDocs, dokList))
        addVedlegginfoToDocument(mergeContext.document, dokList)
        mergeContext.document.newPage()
    }

    private fun createDocInfoLine(docNum: Int, totalDocs: Int, docinfo: Dokumentinfo): Paragraph {
        val innhold = if (docinfo.dokumentnavn == null) "" else ": " + docinfo.dokumentnavn
        return createCenterInfoParagraph(
            "Dokument nr $docNum av $totalDocs$innhold"
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

    private fun addVedlegginfoToDocument(document: Document, documentinfo: Dokumentinfo) {
        if (documentinfo.vedleggListe?.isNotEmpty() == true) {
            val spacer = Paragraph(" ")
            spacer.setSpacingAfter((SPACING / 2).toFloat())
            document.add(spacer)
            val paragraph = createCenterInfoParagraph("Vedlegg: ")
            document.add(paragraph)
            for (vedlegg in documentinfo.vedleggListe) {
                document.add(createCenterInfoParagraph(vedlegg.dokumentnavn))
            }
        }
    }
}
