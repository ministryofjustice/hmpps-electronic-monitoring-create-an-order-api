package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class RiskCategory(val value: String) {
  THREATS_OF_VIOLENCE("Threats of Violence"),
  SEXUAL_OFFENCES("Sexual Offences"),
  RISK_TO_GENDER("Risk to Specific Gender"),
  RACIAL_ABUSE_OR_THREATS("Racial Abuse or Threats"),
  HISTORY_OF_SUBSTANCE_ABUSE("History of Substance Abuse"),
  DIVERSITY_CONCERNS("Diversity Concerns (mental health issues, learning difficulties etc.)"),
  DANGEROUS_ANIMALS("Dangerous Dogs/Pets at Premises"),
  IOM("Is the Subject managed through IOM?"),
  SAFEGUARDING_ISSUE("Safeguarding Issues"),
  OTHER_OCCUPANTS("Other occupants who pose a risk to staff"),
  OTHER_RISKS("Other known Risks"),
  HOMOPHOBIC_VIEWS("Is there evidence known to the subject having homophobic views"),
  OFFENCE_RISK("Offence Risk"),
  POSTCODE_RISK("Postcode Risk"),
}
