package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections

interface DeviceWearerListInformation {
  fun getFirstName(): String?
  fun getLastName(): String?
}
