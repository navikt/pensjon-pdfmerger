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
        info.gjelderName,
        mapDocumentsinfoRequestToDomainAndValidate(info, documents)
    )
}

fun mapDocumentsinfoRequestToDomainAndValidate(
    info: MergeInfoRequest,
    documents: MutableMap<String, ByteArray>
): List<Documentinfo> {
    val documentinfo = mutableListOf<Documentinfo>()
    info.documentinfo.forEach {
        documentinfo.add(
            Documentinfo(
                it.filename,
                it.dokumenttype,
                it.fagomrade,
                it.saknr,
                it.avsenderMottaker,
                it.documentName,
                mapStringToDate(it.mottattSendtDato),
                findFile(it.filename, documents),
                mapVedleggListRequestToDomainAndValidate(it.vedleggList, documents)
            )
        )
    }

    documentinfo.sortWith(Comparator.comparing(Documentinfo::mottattSendtDato))
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
                it.filename,
                it.documentName,
                findFile(it.filename, documents)
            )
        )
    }
    return vedlegglist.toList()
}

fun findFile(filename: String, documents: MutableMap<String, ByteArray>): ByteArray {
    val file = documents.get(filename)
    if (file == null) {
        throw BadRequestException("Missing file that is present in request $filename")
    }
    return file
}

fun mapStringToDate(mottattSendtDato: String): LocalDate {
    return LocalDate.parse(mottattSendtDato, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
