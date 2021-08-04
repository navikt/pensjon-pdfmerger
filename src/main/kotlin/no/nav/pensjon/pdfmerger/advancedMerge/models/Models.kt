package no.nav.pensjon.pdfmerger

import java.time.LocalDate

data class MergeInfo(
    val gjelderID: String,
    val gjelderName: String,
    val documentinfo: List<Documentinfo>)

data class Documentinfo(
    val filename: String,
    val dokumenttype: String,
    val fagomrade: String,
    val saknr: String,
    val avsenderMottaker: String,
    val documentName: String,
    val mottattSendtDato: LocalDate,
    val file: ByteArray,
    val vedleggList: List<VedleggDokument>)

data class VedleggDokument(
    val filename: String,
    val documentName: String,
    val file: ByteArray)
