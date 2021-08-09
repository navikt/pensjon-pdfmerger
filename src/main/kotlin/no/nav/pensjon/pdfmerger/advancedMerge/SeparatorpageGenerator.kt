package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import no.nav.pensjon.pdfmerger.Dokumentinfo

val PAGE_MARGIN = 10.0f

class SeparatorpageGenerator(private val document: Document) {

    fun createSeparatorPage(
        docNr: Int,
        totalDocs: Int,
        dokList: Dokumentinfo,
        gjelderNavn: String
    ) {
        document.add(EMPTY_PARAGRAPH)
        val heading = Paragraph(gjelderNavn, INFO_FONT_BOLD)
        heading.setSpacingAfter(SPACING.toFloat())
        heading.setAlignment(Paragraph.ALIGN_CENTER)
        document.add(heading)
        document.add(createDocInfoLine(docNr, totalDocs, dokList))
        addVedlegginfoToDocument(dokList)
        document.newPage()
    }

    private fun createDocInfoLine(docNum: Int, totalDocs: Int, docinfo: Dokumentinfo): Paragraph {
        val innhold = if (docinfo.dokumentnavn.isEmpty()) ": " + docinfo.dokumentnavn else ""
        return createCenterInfoParagraph(
            "Dokument nr " + docNum + " av " + totalDocs + innhold
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
