package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Cohorts

class UserCohortService {

  fun getUserCohort(authentication: Authentication): UserCohort {
    if (authentication.authorities.any { it.authority == "ROLE_PRISON" }) {
      return UserCohort(Cohorts.PRISON)
    } else if (authentication.authorities.any { it.authority == "ROLE_PROBATION" }) {
      return UserCohort(Cohorts.PROBATION)
    }
    return UserCohort(Cohorts.OTHER)
  }
}
