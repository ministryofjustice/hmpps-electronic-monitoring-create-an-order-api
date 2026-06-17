package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserRole
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5

@Service
class UserCohortService(private val webClient: ManageUserApi) {

  fun getUserCohort(authentication: JwtAuthenticationToken): UserCohort {
    val roles = authentication.authorities.map { it.authority }.toSet()

    return when {
      UserRole.HOME_OFFICE.code in roles -> UserCohort(Cohort.HOME_OFFICE)
      UserRole.COURT.code in roles -> UserCohort(Cohort.COURT)
      UserRole.PRISON.code in roles -> {
        val caseload = webClient.getUserActiveCaseload(authentication.token)
        UserCohort(Cohort.PRISON, caseload?.name, caseload?.id)
      }

      UserRole.PROBATION.code in roles -> UserCohort(Cohort.PROBATION)
      else -> UserCohort(Cohort.OTHER)
    }
  }

  fun matchesNofifyingOrg(cohort: Cohort, notifyingOrganisation: String?): Boolean {
    if (notifyingOrganisation.isNullOrEmpty()) return false

    val parsedNotifyingOrganisation = NotifyingOrganisationDDv5.from(notifyingOrganisation)
    return when (cohort) {
      Cohort.PRISON ->
        parsedNotifyingOrganisation == NotifyingOrganisationDDv5.PRISON ||
          parsedNotifyingOrganisation == NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE
      Cohort.PROBATION -> parsedNotifyingOrganisation == NotifyingOrganisationDDv5.PROBATION
      Cohort.COURT -> NotifyingOrganisationDDv5.isCourt(notifyingOrganisation)
      Cohort.HOME_OFFICE -> parsedNotifyingOrganisation == NotifyingOrganisationDDv5.HOME_OFFICE
      Cohort.OTHER -> false
    }
  }
}
