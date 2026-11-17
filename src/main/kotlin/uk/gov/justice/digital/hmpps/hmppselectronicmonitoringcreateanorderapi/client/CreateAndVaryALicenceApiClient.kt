package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.Licence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceSummary

@Component
class CreateAndVaryALicenceApiClient(private val createAndVaryALicenceApiWebClient: WebClient) :
  CreateAndVaryALicenceApi {

  val crnSearch: String = "/public/licence-summaries/crn/"
  val prisonSearch: String = "/public/licence-summaries/prison-number/"

  override fun getLicenceByCrn(crn: String): LicenceSummary? = getLicenceSummary(crnSearch, crn)

  override fun getLicenceByPrisonNumber(prisonNumber: String): LicenceSummary? =
    getLicenceSummary(prisonSearch, prisonNumber)

  private fun getLicenceSummary(searchUrl: String, searchKey: String): LicenceSummary? {
    val licenceSummaryList: List<LicenceSummary> = createAndVaryALicenceApiWebClient.get()
      .uri("${searchUrl}$searchKey")
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<List<LicenceSummary>>() {})
      .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
      .block()
      ?: emptyList()

    return licenceSummaryList
      .filter { it.statusCode == "APPROVED" }
      .maxByOrNull { it.approvedDateTime }
  }

  override fun getLicenceConditions(licenceId: String): Licence? = createAndVaryALicenceApiWebClient.get()
    .uri("/public/v2/licences/id/$licenceId")
    .retrieve()
    .bodyToMono(object : ParameterizedTypeReference<Licence>() {})
    .onErrorResume(WebClientResponseException::class.java) { Mono.empty() }
    .block()
}
