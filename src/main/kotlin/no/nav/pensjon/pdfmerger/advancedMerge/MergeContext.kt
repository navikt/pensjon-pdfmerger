package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream

class MergeContext {
    val document: Document = Document()
    val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    val pdfWriter: PdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream)

    companion object {
        val SPACING = 50
        val INFO_FONT_BOLD: Font = Font(Font.TIMES_ROMAN, 18f, Font.BOLD)
        val PAGE_MARGIN = 10.0f
        val EMPTY_PARAGRAPH = Paragraph(" ").apply {
            setSpacingAfter(SPACING.toFloat())
        }
    }
}
