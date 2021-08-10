package no.nav.pensjon.pdfmerger.advancedMerge

import io.ktor.features.*
import no.nav.pensjon.pdfmerger.advancedMerge.models.*
import java.util.Comparator

fun mapRequestToDomain(
    info: MergeInfoRequest
): MergeInfo {
    return MergeInfo(
        info.gjelderID,
        info.gjelderNavn,
        mapDokumentinfoRequestToDomain(info.dokumentinfo)
    )
}

fun mapDokumentinfoRequestToDomain(
    dokumentinfoRequest: List<DokumentinfoRequest>
): List<Dokumentinfo> {
    val dokumentinfo = mutableListOf<Dokumentinfo>()
    dokumentinfoRequest.forEach {
        dokumentinfo.add(
            Dokumentinfo(
                filnavn = it.filnavn,
                dokumenttype = it.dokumenttype,
                fagomrade = it.fagomrade,
                saknr = it.saknr,
                avsenderMottaker = it.avsenderMottaker,
                dokumentnavn = it.dokumentnavn,
                mottattSendtDato = it.mottattSendtDato,
                vedleggListe = mapVedleggListRequestToDomain(it.vedleggListe)
            )
        )
    }

    dokumentinfo.sortWith(Comparator.comparing(Dokumentinfo::mottattSendtDato))
    return dokumentinfo.toList()
}

fun mapVedleggListRequestToDomain(
    vedleggListRequest: List<VedleggDokumentRequest>?
): List<VedleggDokument> {
    val vedlegglist = mutableListOf<VedleggDokument>()
    vedleggListRequest?.forEach {
        vedlegglist.add(
            VedleggDokument(
                it.filnavn,
                it.dokumentnavn
            )
        )
    }
    return vedlegglist.toList()
}

fun findFileIfGiven(filnavn: String, documents: MutableMap<String, ByteArray>): ByteArray? {
    if (filnavn.isEmpty()) {
        return null
    }
    return findFile(filnavn, documents)
}

fun findFile(filename: String, documents: MutableMap<String, ByteArray>): ByteArray {
    val file = documents.get(filename)
        ?: throw BadRequestException("Missing file that is present in request $filename")
    return file
}
