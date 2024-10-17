package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.apache.tomcat.util.json.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SercoConnectionException
import java.util.*

@Component
class FmsAuthClient(
  @Value("\${services.serco.auth-url}") authUrl: String,
  @Value("\${services.serco.client-id}") clientId: String,
  @Value("\${services.serco.client-secret}") clientSecret: String,
  @Value("\${services.serco.username}") val username: String,
  @Value("\${services.serco.password}") val password: String,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(authUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).build()
  private val encodedCredentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

  fun getClientToken(): String {
    val response =
      webClient
        .post()
        .uri("")
        .body(
          BodyInserters.fromFormData("username", username)
            .with("password", password)
            .with("grant_type", "password"),
        )
        .header("Authorization", "Basic $encodedCredentials")
        .retrieve()
        .onStatus({ t -> t.value() == 403 }, { Mono.error(SercoConnectionException("Invalid credentials used.")) })
        .onStatus({ t -> t.value() == 503 }, { Mono.error(SercoConnectionException("Serco authentication service is unavailable.")) })
        .bodyToMono(String::class.java)
        .block()

    return JSONParser(response).parseObject()["access_token"].toString()
  }
}
