package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserRole

@Service
class UserCohortService(private val webClient: ManageUserApi) {

  fun getUserCohort(authentication: JwtAuthenticationToken): UserCohort {
    val roles = authentication.authorities.map { it.authority }.toSet()

    return when {
      UserRole.HOME_OFFICE.code in roles -> UserCohort(Cohort.HOME_OFFICE)
      UserRole.COURT.code in roles -> UserCohort(Cohort.COURT)
      UserRole.PRISON.code in roles -> {
        val caseload = webClient.getUserActiveCaseloadName(authentication.token)
        UserCohort(Cohort.PRISON, caseload)
      }
      UserRole.PROBATION.code in roles -> UserCohort(Cohort.PROBATION)
      else -> UserCohort(Cohort.OTHER)
    }
  }
}
