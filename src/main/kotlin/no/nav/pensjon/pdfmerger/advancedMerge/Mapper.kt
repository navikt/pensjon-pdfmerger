package no.nav.pensjon.pdfmerger.advancedMerge

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
