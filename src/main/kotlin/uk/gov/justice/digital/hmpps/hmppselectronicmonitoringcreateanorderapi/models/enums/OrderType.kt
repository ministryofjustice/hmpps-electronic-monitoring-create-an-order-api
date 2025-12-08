package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class OrderType(val value: String) {
  CIVIL("Civil"),
  COMMUNITY("Community"),
  IMMIGRATION("Immigration"),
  POST_RELEASE("Post Release"),
  PRE_TRIAL("Pre-Trial"),
  SPECIAL("Special"),
  BAIL("Bail"),
}
