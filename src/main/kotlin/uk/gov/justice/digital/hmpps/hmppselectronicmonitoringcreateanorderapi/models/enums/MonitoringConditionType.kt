package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class MonitoringConditionType(val value: String) {
  REQUIREMENT_OF_A_COMMUNITY_ORDER("Requirement of a Community Order"),
  LICENSE_CONDITION_OF_A_CUSTODIAL_ORDER("License Condition of a Custodial Order"),
  POST_SENTENCE_SUPERVISION_REQUIREMENT("Post-Sentence Supervision Requirement following on from an Adult Custody order"),
  BAIL_ORDER("Bail Order"),
}
