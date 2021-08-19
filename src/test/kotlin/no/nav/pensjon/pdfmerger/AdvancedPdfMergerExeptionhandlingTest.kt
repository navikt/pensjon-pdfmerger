package no.nav.pensjon.pdfmerger

import io.ktor.features.*
import no.nav.pensjon.pdfmerger.advancedMerge.AdvancedPdfMerger
import no.nav.pensjon.pdfmerger.advancedMerge.MergeRequest
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokument
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDate
import kotlin.test.assertFailsWith

class AdvancedPdfMergerExeptionhandlingTest {
    private val pdfMerger = AdvancedPdfMerger()
    private val documentA = readTestResource("/a.pdf")
    private val invalidDocument = readTestResource("/not_a_pdf")

    @Test
    fun `mergeWithSeparator with missing file throws BadRequestExtection`() {
        assertFailsWith(
            BadRequestException::class,
            {
                pdfMerger.merge(
                    MergeRequest(
                        mockMergeinfo(listOf("a.pdf", "b.pdf")),
                        mapOf("a.pdf" to documentA)
                    )
                )
            }
        )
    }

    @Test
    fun `mergeWithSeparator with invalid document throws IOException`() {
        assertFailsWith(
            IOException::class,
            {
                pdfMerger.merge(
                    MergeRequest(
                        mockMergeinfo(listOf("a.pdf", "not_a_pdf")),
                        mapOf(
                            "a.pdf" to documentA,
                            "not_a_pdf" to invalidDocument
                        )
                    )
                )
            }
        )
    }

    private fun mockMergeinfo(hoveddokument: List<String>): MergeInfo {
        return MergeInfo("Mitt Navn", "0101202012345", mockDokumentinfo(hoveddokument))
    }

    private fun mockDokumentinfo(hoveddokumenter: List<String>): List<Dokumentinfo> {
        return hoveddokumenter.map {
            Dokumentinfo(
                dokumenttype = "I",
                fagomrade = "Uføre",
                saknr = "2000",
                mottattSendtDato = LocalDate.now(),
                vedleggListe = listOf(),
                hoveddokument = Dokument(
                    it,
                    "Innhold i hoveddokument $it"
                )
            )
        }
    }

    private fun readTestResource(name: String) =
        AdvancedPdfMergerExeptionhandlingTest::class.java.getResourceAsStream(name)?.readBytes()
            ?: throw FileNotFoundException("Missing testfile $name")
}
