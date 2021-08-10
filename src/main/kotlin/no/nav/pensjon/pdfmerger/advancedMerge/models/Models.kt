package no.nav.pensjon.pdfmerger

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate

data class MergeInfo(
    val gjelderID: String,
    val gjelderNavn: String,
    val dokumentinfo: List<Dokumentinfo>
)

data class Dokumentinfo(
    val filnavn: String,
    val dokumenttype: String,
    val fagomrade: String,
    val saknr: String,
    val avsenderMottaker: String? = "",
    val dokumentnavn: String,
    val mottattSendtDato: LocalDate,
    val vedleggListe: List<VedleggDokument>,
    @JsonIgnore
    val fil: ByteArray?
)

data class VedleggDokument(
    val filnavn: String,
    val dokumentnavn: String,
    @JsonIgnore
    val fil: ByteArray
)
