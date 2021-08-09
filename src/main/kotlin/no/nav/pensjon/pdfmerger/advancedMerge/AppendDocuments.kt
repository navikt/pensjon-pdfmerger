package no.nav.pensjon.pdfmerger.advancedMerge

import com.lowagie.text.Document
import com.lowagie.text.Image
import com.lowagie.text.PageSize
import com.lowagie.text.pdf.PdfImportedPage
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfWriter
import no.nav.pensjon.pdfmerger.Dokumentinfo

class AppendDocuments(private val document: Document, private val pdfWriter: PdfWriter) {

    fun appendDocumentWithVedlegg(documentinfo: Dokumentinfo) {
        val files: MutableList<ByteArray> = ArrayList()

        files.add(documentinfo.fil)
        documentinfo.vedleggListe.forEach { files.add(it.fil) }

        for (file in files) {
            document.setMargins(0f, 0f, -14f, 0f)
            val reader = PdfReader(file)
            var page: PdfImportedPage
            for (p in 1..reader.numberOfPages) {
                page = pdfWriter.getImportedPage(reader, p)
                val image = Image.getInstance(page)
                image.scaleToFit(PageSize.A4.width(), PageSize.A4.height())
                document.add(image)
                if (p < reader.numberOfPages) {
                    document.newPage()
                }
            }
            document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN)
            document.newPage()
            pdfWriter.freeReader(reader)
            reader.close()
        }
    }
}
