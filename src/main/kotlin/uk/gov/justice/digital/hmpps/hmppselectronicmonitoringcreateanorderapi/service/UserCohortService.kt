package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohorts
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort

@Service
class UserCohortService(private val webClient: ManageUserApi) {

  fun getUserCohort(authentication: JwtAuthenticationToken): UserCohort {
    if (authentication.authorities.any { it.authority == "ROLE_PRISON" }) {
      val activeCaseLoad = webClient.getUserActiveCaseload(authentication.token)
      return UserCohort(Cohorts.PRISON, activeCaseLoad)
    } else if (authentication.authorities.any { it.authority == "ROLE_PROBATION" }) {
      return UserCohort(Cohorts.PROBATION)
    }
    // TODO: set cohort for Courts and HO users
    return UserCohort(Cohorts.OTHER)
  }
}
