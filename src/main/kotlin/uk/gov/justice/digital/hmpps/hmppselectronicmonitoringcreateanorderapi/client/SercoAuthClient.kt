package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.apache.tomcat.util.json.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exceptions.SercoConnectionException
import java.util.*

@Component
class SercoAuthClient(
  @Value("\${services.serco.auth-url}") authUrl: String,
  @Value("\${services.serco.client-id}") clientId: String,
  @Value("\${services.serco.client-secret}") clientSecret: String,
  @Value("\${services.serco.username}") val username: String,
  @Value("\${services.serco.password}") val password: String,
) {
  private val webClient: WebClient = WebClient.builder().baseUrl(authUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).build()
  private val encodedCredentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

  fun getClientToken(): String {

    return try {
      val response =
        webClient
          .post()
          .uri("")
          .body(
            BodyInserters.fromFormData("username",username )
              .with("password", password)
              .with("grant_type","password"))
          .header("Authorization", "Basic $encodedCredentials")
          .retrieve()
          .bodyToMono(String::class.java)
          .block()

      JSONParser(response).parseObject()["access_token"].toString()
    } catch (exception: WebClientRequestException) {
      throw SercoConnectionException("Connection to ${exception.uri.authority} failed .")
    } catch (exception: WebClientResponseException.ServiceUnavailable) {
      throw SercoConnectionException("${exception.request?.uri?.authority} is unavailable.")
    } catch (exception: WebClientResponseException.Unauthorized) {
      throw SercoConnectionException("Invalid credentials used.")
    }
  }
}