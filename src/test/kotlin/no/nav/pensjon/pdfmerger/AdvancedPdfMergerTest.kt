package no.nav.pensjon.pdfmerger

import no.nav.pensjon.pdfmerger.advancedMerge.AdvancedPdfMerger
import no.nav.pensjon.pdfmerger.advancedMerge.MergeRequest
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokument
import no.nav.pensjon.pdfmerger.advancedMerge.models.Dokumentinfo
import no.nav.pensjon.pdfmerger.advancedMerge.models.MergeInfo
import org.apache.pdfbox.pdmodel.PDDocument.load
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.FileNotFoundException
import java.time.LocalDate
import kotlin.test.assertEquals

class AdvancedPdfMergerTest {
    private val pdfMerger = AdvancedPdfMerger()

    @ParameterizedTest
    @MethodSource("MergeinputAndPageCountOnResult")
    fun `test that mergeWithSeparator returns the pagenumbers as expected`(
        mergeRequest: MergeRequest,
        expectedResult: Int,
        msg: String
    ) {
        val mergedDocument = pdfMerger.merge(mergeRequest)
        assertEquals(expectedResult, load(mergedDocument).numberOfPages, msg)
    }

    companion object {
        private val documentA = readTestResource("/a.pdf")
        private val documentB = readTestResource("/b.pdf")
        private val documentVedleggA = readTestResource("/vedleggA.pdf")
        private val documentVedleggB = readTestResource("/vedleggB.pdf")

        @JvmStatic
        fun MergeinputAndPageCountOnResult() = listOf(
            Arguments.of(
                MergeRequest(
                    mockMergeinfo(listOf("a.pdf", "b.pdf"), listOf()),
                    mapOf("a.pdf" to documentA, "b.pdf" to documentB)
                ),
                load(documentA).numberOfPages + load(documentB).numberOfPages + 3,
                "The merged document should have the same page count as the sum of pages " +
                    "of the input documents + one frontpage and two separatorpages"
            ),
            Arguments.of(
                MergeRequest(
                    mockMergeinfo(listOf("a.pdf", "b.pdf"), listOf("vedleggA.pdf", "vedleggB.pdf")),
                    mapOf(
                        "a.pdf" to documentA,
                        "b.pdf" to documentB,
                        "vedleggA.pdf" to documentVedleggA,
                        "vedleggB.pdf" to documentVedleggB
                    )
                ),
                load(documentA).numberOfPages + load(documentVedleggA).numberOfPages +
                    load(documentVedleggB).numberOfPages + load(documentB).numberOfPages + 3,
                "The merged document should have the same page count as the sum of pages " +
                    "of the two hoveddokumentene and the two vedleggene + one frontpage and two separatorpages"
            ),
            Arguments.of(
                MergeRequest(
                    mockMergeinfoWithoutHoveddokument(listOf("vedleggA.pdf", "vedleggB.pdf")),
                    mapOf("vedleggA.pdf" to documentVedleggA, "vedleggB.pdf" to documentVedleggB)
                ),
                load(documentVedleggA).numberOfPages +
                    load(documentVedleggB).numberOfPages + 2,
                "The merged document should have the same page count as the sum of pages " +
                    "of the input vedleggene + one frontpage and one separatorpage"
            )
        )

        private fun mockMergeinfo(hoveddokument: List<String>, vedlegg: List<String>): MergeInfo {
            return MergeInfo("Mitt Navn", "0101202012345", mockDokumentinfo(hoveddokument, vedlegg))
        }

        private fun mockMergeinfoWithoutHoveddokument(vedlegg: List<String>): MergeInfo {
            return MergeInfo(
                "Mitt Navn",
                "0101202012345",
                mockDokumentinfoWithoutHoveddokument(vedlegg)
            )
        }

        private fun mockDokumentinfo(hoveddokumenter: List<String>, vedlegg: List<String>): List<Dokumentinfo> {
            return listOf(
                Dokumentinfo(
                    dokumenttype = "I",
                    fagomrade = "Uføre",
                    saknr = "2000",
                    mottattSendtDato = LocalDate.now(),
                    vedleggListe = mockVedlegg(vedlegg),
                    hoveddokument = mockHoveddokument(
                        hoveddokumenter.get(0),
                        "Innhold i hoveddokument ${hoveddokumenter.get(0)}"
                    )
                ),
                Dokumentinfo(
                    "U",
                    "Uføre",
                    "2000",
                    "Bruker",
                    LocalDate.now(),
                    listOf(),
                    mockHoveddokument(
                        hoveddokumenter.get(1),
                        "Innhold i hoveddokument ${hoveddokumenter.get(1)}"
                    )
                )
            )
        }

        private fun mockDokumentinfoWithoutHoveddokument(vedlegg: List<String>): List<Dokumentinfo> {
            return listOf(
                Dokumentinfo(
                    dokumenttype = "I",
                    fagomrade = "Uføre",
                    saknr = "2000",
                    avsenderMottaker = null,
                    mottattSendtDato = LocalDate.now(),
                    vedleggListe = mockVedlegg(vedlegg)
                )
            )
        }

        private fun mockVedlegg(vedlegg: List<String>): List<Dokument> {
            val vedleggDokumenter = mutableListOf<Dokument>()
            vedlegg.forEach {
                vedleggDokumenter.add(Dokument(it, "Innhold i vedlegg $it"))
            }
            return vedleggDokumenter.toList()
        }

        private fun mockHoveddokument(filnavn: String, innhold: String): Dokument {
            return Dokument(filnavn, innhold)
        }

        private fun readTestResource(name: String) =
            AdvancedPdfMergerTest::class.java.getResourceAsStream(name)?.readBytes()
                ?: throw FileNotFoundException("Missing testfile $name")
    }
}
