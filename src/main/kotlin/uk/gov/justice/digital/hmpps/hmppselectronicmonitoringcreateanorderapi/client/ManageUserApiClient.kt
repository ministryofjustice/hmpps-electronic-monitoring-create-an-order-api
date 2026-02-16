package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserGroup
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserCaseloadResponse

@Component
class ManageUserApiClient(private val manageUserApiWebClient: WebClient) : ManageUserApi {

  override fun getUserGroups(token: Jwt): List<UserGroup> = manageUserApiWebClient
    .get()
    .uri("/users/me/groups")
    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.tokenValue}")
    .retrieve()
    .bodyToMono(object : ParameterizedTypeReference<List<UserGroup>>() {})
    .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
    .block()
    ?: emptyList()

  override fun getUserActiveCaseloadName(token: Jwt): String? = manageUserApiWebClient
    .get()
    .uri("/users/me/caseloads")
    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.tokenValue}")
    .retrieve()
    .bodyToMono(HmppsUserCaseloadResponse::class.java)
    .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
    .block()!!
    .activeCaseload?.name
}
