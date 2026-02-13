package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.security.oauth2.jwt.Jwt

interface ManageUserApi {
  fun getUserGroups(token: Jwt): List<String>
  fun getUserActiveCaseload(token: Jwt): String?
}
