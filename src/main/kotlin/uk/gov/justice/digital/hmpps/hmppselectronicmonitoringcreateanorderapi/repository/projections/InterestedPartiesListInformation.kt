package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections

interface InterestedPartiesListInformation {
  fun getNotifyingOrganisation(): String?
}
