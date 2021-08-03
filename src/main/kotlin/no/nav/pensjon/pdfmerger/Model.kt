package no.nav.pensjon.pdfmerger

data class Documentinfo(val documentName: String, val vedleggNames: List<String>?)

data class MergeInfo(val gjelderID: String, val gjelderName: String, val documentinfo: List<Documentinfo>)