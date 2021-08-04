package no.nav.pensjon.pdfmerger

data class Documentinfo(val filename: String, val dokumenttype: String, val fagomrade: String,
                        val saknr: String, val avsenderMottaker: String, val documentName: String,
                        val mottattSendtDato: String, val vedleggList: List<VedleggDokument>?)

data class VedleggDokument(val filename: String, val documentName: String)

data class MergeInfo(val gjelderID: String, val gjelderName: String,
                     val documentinfo: List<Documentinfo>)