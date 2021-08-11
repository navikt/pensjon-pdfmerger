package no.nav.pensjon.pdfmerger.advancedMerge

import io.ktor.features.*
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo

data class MergeRequest(
    private val mergeinfo: MergeInfo,
    private val documents: MutableMap<String, ByteArray>
) {
    val gjelderID: String = mergeinfo.gjelderID
    val gjelderNavn: String = mergeinfo.gjelderNavn
    val dokumentinfo: List<Dokumentinfo> = mergeinfo.dokumentinfo
    val totalDocs: Int = dokumentinfo.size

    fun findFiles(documentinfo: Dokumentinfo) = listOfNotNull(
        documentinfo.filnavn?.let {
            listOf(findFile(it))
        },

        documentinfo.vedleggListe?.map {
            findFile(it.filnavn)
        }
    ).flatten()

    private fun findFile(filename: String): ByteArray {
        return documents[filename]
            ?: throw BadRequestException("Missing file that is present in request $filename")
    }
}
