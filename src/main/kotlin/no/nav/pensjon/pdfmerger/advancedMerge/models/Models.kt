package no.nav.pensjon.pdfmerger.advancedMerge.models

import java.time.LocalDate

data class MergeInfo(
    val gjelderID: String,
    val gjelderNavn: String,
    val dokumentinfo: List<Dokumentinfo>
)

data class Dokumentinfo(
    val dokumenttype: String,
    val fagomrade: String,
    val saknr: String,
    val avsenderMottaker: String? = null,
    val mottattSendtDato: LocalDate,
    val vedleggListe: List<Dokument>,
    val hoveddokument: Dokument? = null
) {

    fun getSortedDokumenter(): List<Dokument> {
        if (hoveddokument != null) {
            return listOf(hoveddokument) + vedleggListe
        } else {
            return vedleggListe
        }
    }
}

data class Dokument(
    val filnavn: String,
    val dokumentnavn: String,
)
