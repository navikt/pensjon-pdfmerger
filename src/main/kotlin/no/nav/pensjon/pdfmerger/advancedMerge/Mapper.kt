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

private fun mapDokumentinfoRequestToDomain(
    dokumentinfoRequest: List<DokumentinfoRequest>
): List<Dokumentinfo> {
    val dokumentinfo = mutableListOf<Dokumentinfo>()
    dokumentinfoRequest.forEach {
        dokumentinfo.add(
            Dokumentinfo(
                dokumenttype = it.dokumenttype,
                fagomrade = it.fagomrade,
                saknr = it.saknr,
                avsenderMottaker = it.avsenderMottaker,
                mottattSendtDato = it.mottattSendtDato,
                hoveddokument = mapHoveddokument(it.hoveddokument),
                vedleggListe = mapVedleggListRequestToDomain(it.vedleggListe)
            )
        )
    }

    dokumentinfo.sortWith(Comparator.comparing(Dokumentinfo::mottattSendtDato))
    return dokumentinfo.toList()
}

private fun mapHoveddokument(hoveddokument: DokumentRequest?): Dokument? {
    if (hoveddokument != null) {
        return Dokument(hoveddokument.filnavn, hoveddokument.dokumentnavn)
    } else {
        return null
    }
}

private fun mapVedleggListRequestToDomain(
    vedleggListRequest: List<DokumentRequest>?
): List<Dokument> {
    val vedlegglist = mutableListOf<Dokument>()
    vedleggListRequest?.forEach {
        vedlegglist.add(
            Dokument(
                it.filnavn,
                it.dokumentnavn
            )
        )
    }
    return vedlegglist.toList()
}
