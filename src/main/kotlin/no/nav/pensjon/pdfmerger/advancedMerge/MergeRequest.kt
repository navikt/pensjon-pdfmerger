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

    fun findFileIfGiven(filnavn: String): ByteArray? {
        if (filnavn.isEmpty()) {
            return null
        }
        return findFile(filnavn)
    }

    fun findFile(filename: String): ByteArray {
        return documents[filename]
            ?: throw BadRequestException("Missing file that is present in request $filename")
    }
}
