package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCaseLoad

@Component
class ManageUserApiClient(private val manageUserApiWebClient: WebClient) {

  fun getUserGroups(): List<String> = emptyList()

  fun getUserActiveCaseload(token: Jwt): UserCaseLoad {
    val result = manageUserApiWebClient
      .get()
      .uri("/users/me/caseloads")
      .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.tokenValue}")
      .retrieve()
      .bodyToMono(UserCaseLoad::class.java)
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()!!
    return result
  }
}
