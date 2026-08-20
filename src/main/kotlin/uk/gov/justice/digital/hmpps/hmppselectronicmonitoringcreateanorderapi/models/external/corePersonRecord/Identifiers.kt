package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class Identifiers(
  val crns: List<String> = emptyList(),
  val prisonNumbers: List<String> = emptyList(),
  val pncs: List<String> = emptyList(),
  val nationalInsuranceNumbers: List<String> = emptyList(),
)
