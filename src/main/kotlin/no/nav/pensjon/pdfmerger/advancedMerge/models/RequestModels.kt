package no.nav.pensjon.pdfmerger

data class MergeInfoRequest(
        val gjelderID: String,
        val gjelderNavn: String,
        val dokumentinfo: List<DokumentinfoRequest>
)

data class DokumentinfoRequest(
        val filnavn: String,
        val dokumenttype: String,
        val fagomrade: String,
        val saknr: String,
        val avsenderMottaker: String,
        val dokumentnavn: String,
        val mottattSendtDato: String,
        val vedleggListe: List<VedleggDokumentRequest>?
)

data class VedleggDokumentRequest(
        val filnavn: String,
        val dokumentnavn: String
)
