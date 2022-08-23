package no.nav.pensjon.pdfmerger.advancedMerge

import io.ktor.server.plugins.*
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo

data class MergeRequest(
    private val mergeinfo: MergeInfo,
    private val documents: Map<String, ByteArray>
) {
    val gjelderID: String = mergeinfo.gjelderID
    val gjelderNavn: String = mergeinfo.gjelderNavn
    val dokumentinfo: List<Dokumentinfo> = mergeinfo.dokumentinfo
    val totalDokumentinfo: Int = dokumentinfo.size

    fun findFiles(dokumentinfo: Dokumentinfo) = listOfNotNull(
        dokumentinfo.hoveddokument?.let {
            listOf(findFile(it.filnavn))
        },

        dokumentinfo.vedleggListe.map {
            findFile(it.filnavn)
        }
    ).flatten()

    private fun findFile(filename: String): ByteArray {
        return documents[filename]
            ?: throw BadRequestException("Missing file that is present in request $filename")
    }
}
