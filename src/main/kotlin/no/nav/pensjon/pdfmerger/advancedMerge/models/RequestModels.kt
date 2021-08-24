package no.nav.pensjon.pdfmerger.advancedMerge.models

import java.time.LocalDate

data class MergeInfoRequest(
    val gjelderID: String,
    val gjelderNavn: String,
    val dokumentinfo: List<DokumentinfoRequest>
)

data class DokumentinfoRequest(
    val dokumenttype: String,
    val fagomrade: String,
    val saknr: String,
    val avsenderMottaker: String?,
    val mottattSendtDato: LocalDate,
    val vedleggListe: List<DokumentRequest>?,
    val hoveddokument: DokumentRequest?
)

data class DokumentRequest(
    val filnavn: String,
    val dokumentnavn: String
)
