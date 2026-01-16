package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth

data class UserCohort(val cohort: Cohorts, val activeCaseLoad: String? = "")
