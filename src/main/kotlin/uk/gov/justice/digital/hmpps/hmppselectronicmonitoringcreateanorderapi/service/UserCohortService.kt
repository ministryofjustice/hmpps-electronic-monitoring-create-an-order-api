package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.toCohort

@Service
class UserCohortService(private val webClient: ManageUserApi) {

  fun getUserCohort(authentication: JwtAuthenticationToken): UserCohort {
    if (authentication.authorities.any { it.authority == "ROLE_PRISON" }) {
      val activeCaseLoad = webClient.getUserActiveCaseloadName(authentication.token)
      return UserCohort(Cohort.PRISON, activeCaseLoad)
    } else if (authentication.authorities.any { it.authority == "ROLE_PROBATION" }) {
      return UserCohort(Cohort.PROBATION)
    }

    val userGroups = webClient.getUserGroups(authentication.token)
    val cohort = userGroups.toCohort()
    return UserCohort(cohort)
  }
}
