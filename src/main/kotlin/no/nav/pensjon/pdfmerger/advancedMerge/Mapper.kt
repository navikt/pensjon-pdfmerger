package no.nav.pensjon.pdfmerger.advancedMerge

import io.ktor.features.*
import no.nav.pensjon.pdfmerger.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Comparator

fun mapRequestToDomainAndValidate(
    info: MergeInfoRequest,
    documents: MutableMap<String, ByteArray>
): MergeInfo {
    return MergeInfo(
        info.gjelderID,
        info.gjelderNavn,
        mapDocumentsinfoRequestToDomainAndValidate(info, documents)
    )
}

fun mapDocumentsinfoRequestToDomainAndValidate(
    info: MergeInfoRequest,
    documents: MutableMap<String, ByteArray>
): List<Dokumentinfo> {
    val documentinfo = mutableListOf<Dokumentinfo>()
    info.dokumentinfo.forEach {
        documentinfo.add(
            Dokumentinfo(
                it.filnavn,
                it.dokumenttype,
                it.fagomrade,
                it.saknr,
                it.avsenderMottaker,
                it.dokumentnavn,
                mapStringToDate(it.mottattSendtDato),
                mapVedleggListRequestToDomainAndValidate(it.vedleggListe, documents),
                findFile(it.filnavn, documents)
            )
        )
    }

    documentinfo.sortWith(Comparator.comparing(Dokumentinfo::mottattSendtDato))
    return documentinfo.toList()
}

fun mapVedleggListRequestToDomainAndValidate(
    vedleggListRequest: List<VedleggDokumentRequest>?,
    documents: MutableMap<String, ByteArray>
): List<VedleggDokument> {
    val vedlegglist = mutableListOf<VedleggDokument>()
    vedleggListRequest?.forEach {
        vedlegglist.add(
            VedleggDokument(
                it.filnavn,
                it.dokumentnavn,
                findFile(it.filnavn, documents)
            )
        )
    }
    return vedlegglist.toList()
}

fun findFile(filename: String, documents: MutableMap<String, ByteArray>): ByteArray {
    val file = documents.get(filename)
        ?: throw BadRequestException("Missing file that is present in request $filename")
    return file
}

fun mapStringToDate(mottattSendtDato: String): LocalDate {
    return LocalDate.parse(mottattSendtDato, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}
