package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class SentenceType(val value: String) {
  EXTENDED_DETERMINATE_SENTENCE("Extended Determinate Sentence"),
  IPP("Imprisonment for Public Protection (IPP)"),
  LIFE_SENTENCE("Life Sentence"),
  SOPC("Section 236A Special Custodial Sentences for Offenders of Particular Concern (SOPC)"),
  EPP("Section 227/228 Extended Sentence for Public Protection (EPP)"),
  SECTION_85_EXTENDED_SENTENCES("Section 85 Extended Sentences"),
  STANDARD_DETERMINATE_SENTENCE("Standard Determinate Sentence"),
  DTO("Detention & Training Order"),
}
