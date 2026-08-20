package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

data class Alias(val firstName: String?, val lastName: String?, val middleNames: String?) {
  fun getAlias(): String = "$firstName $middleNames $lastName"
}
