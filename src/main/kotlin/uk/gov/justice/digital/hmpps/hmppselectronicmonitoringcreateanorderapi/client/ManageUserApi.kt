package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload

interface ManageUserApi {
  fun getUserGroups(token: Jwt): List<UserGroup>
  fun getUserActiveCaseload(token: Jwt): HmppsCaseload?
}
