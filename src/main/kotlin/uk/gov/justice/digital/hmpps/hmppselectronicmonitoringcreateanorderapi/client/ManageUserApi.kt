package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup

interface ManageUserApi {
  fun getUserGroups(token: Jwt): List<UserGroup>
  fun getUserActiveCaseloadName(token: Jwt): String?
}
