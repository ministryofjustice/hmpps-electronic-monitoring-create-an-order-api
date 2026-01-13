package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Cohorts

data class UserCohort(val cohort: Cohorts, val activeCaseLoad: String? = "")
