package no.nav.pensjon.pdfmerger

data class DocumentinfoRequest(val filename: String, val dokumenttype: String, val fagomrade: String,
                               val saknr: String, val avsenderMottaker: String, val documentName: String,
                               val mottattSendtDato: String, val vedleggList: List<VedleggDokumentRequest>?)

data class VedleggDokumentRequest(val filename: String, val documentName: String)

data class MergeInfoRequest(val gjelderID: String, val gjelderName: String,
                            val documentinfo: List<DocumentinfoRequest>)