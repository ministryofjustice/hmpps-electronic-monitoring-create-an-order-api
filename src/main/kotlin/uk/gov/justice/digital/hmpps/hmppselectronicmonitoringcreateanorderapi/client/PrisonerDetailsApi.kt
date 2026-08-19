package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

interface PrisonerDetailsApi {
  fun getPersionDetails(prisonNumber: String)
}
