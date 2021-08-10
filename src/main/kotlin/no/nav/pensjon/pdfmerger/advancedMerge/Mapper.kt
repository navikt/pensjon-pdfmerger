package no.nav.pensjon.pdfmerger.advancedMerge

import io.ktor.features.*
import no.nav.pensjon.pdfmerger.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Comparator

fun mapRequestToDomain(
    info: MergeInfoRequest,
    documents: MutableMap<String, ByteArray>
): MergeInfo {
    return MergeInfo(
        info.gjelderID,
        info.gjelderNavn,
        mapDokumentinfoRequestToDomain(info.dokumentinfo, documents)
    )
}

fun mapDokumentinfoRequestToDomain(
    dokumentinfoRequest: List<DokumentinfoRequest>,
    documents: MutableMap<String, ByteArray>
): List<Dokumentinfo> {
    val dokumentinfo = mutableListOf<Dokumentinfo>()
    dokumentinfoRequest.forEach {
        dokumentinfo.add(
            Dokumentinfo(
                it.filnavn,
                it.dokumenttype,
                it.fagomrade,
                it.saknr,
                it.avsenderMottaker,
                it.dokumentnavn,
                mapStringToDate(it.mottattSendtDato),
                mapVedleggListRequestToDomain(it.vedleggListe, documents),
                findFileIfGiven(it.filnavn, documents)
            )
        )
    }

    dokumentinfo.sortWith(Comparator.comparing(Dokumentinfo::mottattSendtDato))
    return dokumentinfo.toList()
}

fun findFileIfGiven(filnavn: String, documents: MutableMap<String, ByteArray>): ByteArray? {
    if (filnavn.isEmpty()) {
        return null
    }
    return findFile(filnavn, documents)
}

fun mapVedleggListRequestToDomain(
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
