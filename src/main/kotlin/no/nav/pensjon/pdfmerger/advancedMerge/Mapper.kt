package no.nav.pensjon.pdfmerger.advancedMerge

import no.nav.pensjon.pdfmerger.advancedMerge.models.*

fun mapRequestToDomain(info: MergeInfoRequest) = MergeInfo(
    gjelderID = info.gjelderID,
    gjelderNavn = info.gjelderNavn,
    dokumentinfo = mapDokumentinfoRequestToDomain(info.dokumentinfo)
)

private fun mapDokumentinfoRequestToDomain(dokumentinfoRequests: List<DokumentinfoRequest>) = dokumentinfoRequests
    .map {
        Dokumentinfo(
            dokumenttype = it.dokumenttype,
            fagomrade = it.fagomrade,
            saknr = it.saknr,
            avsenderMottaker = it.avsenderMottaker,
            mottattSendtDato = it.mottattSendtDato,
            hoveddokument = it.hoveddokument?.let(::mapDokument),
            vedleggListe = it.vedleggListe?.map(::mapDokument) ?: emptyList()
        )
    }
    .sortedBy(Dokumentinfo::mottattSendtDato)

private fun mapDokument(dokumentRequest: DokumentRequest) =
    Dokument(filnavn = dokumentRequest.filnavn, dokumentnavn = dokumentRequest.dokumentnavn)
