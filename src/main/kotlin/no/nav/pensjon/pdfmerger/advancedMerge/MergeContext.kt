package no.nav.pensjon.pdfmerger.advancedMerge

import org.openpdf.text.Document
import org.openpdf.text.Font
import org.openpdf.text.Paragraph
import org.openpdf.text.pdf.PdfWriter
import org.openpdf.text.pdf.PdfWriter.getInstance
import java.io.ByteArrayOutputStream
import java.io.Closeable

class MergeContext : Closeable {
    val document: Document = Document()
    val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    val pdfWriter: PdfWriter = getInstance(document, byteArrayOutputStream)

    init {
        document.open()
    }

    override fun close() {
        if (document.isOpen) {
            document.close()
        }
    }

    companion object {
        const val SPACING = 50f
        val INFO_FONT_BOLD: Font = Font(Font.TIMES_ROMAN, 18f, Font.BOLD)
        const val PAGE_MARGIN = 10.0f
        val EMPTY_PARAGRAPH = Paragraph(" ").apply {
            setSpacingAfter(SPACING)
        }
    }
}
