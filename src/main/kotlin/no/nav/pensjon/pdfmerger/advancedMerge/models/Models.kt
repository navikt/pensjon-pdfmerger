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
    val hoveddokument: Dokument? = null,
    val vedleggListe: List<Dokument>,
) {
    init {
        require(hoveddokument != null || vedleggListe.isNotEmpty()) { "Må minst ha et hoveddokument eller vedlegg" }
    }

    val hasHovedDokument: Boolean
        get() = hoveddokument != null

    val dokumentnavn: String
        get() = (hoveddokument ?: vedleggListe[0]).dokumentnavn

    val otherDokuments: List<Dokument>
        get() = when {
            hoveddokument != null -> {
                vedleggListe
            }
            vedleggListe.size > 1 -> {
                vedleggListe.drop(1)
            }
            else -> {
                listOf()
            }
        }
}

data class Dokument(
    val filnavn: String,
    val dokumentnavn: String,
)
