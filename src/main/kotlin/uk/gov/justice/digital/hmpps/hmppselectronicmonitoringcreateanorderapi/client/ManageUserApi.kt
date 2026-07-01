package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.UserDetails

interface ManageUserApi {
  fun getUserGroups(token: Jwt): List<UserGroup>
  fun getUserActiveCaseload(token: Jwt): HmppsCaseload?
  fun getUserDetails(authentication: JwtAuthenticationToken): UserDetails?
}
