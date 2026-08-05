package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

data class Up3RetrieveDWandMO(
  val caseId: String,
  val deviceWearer: Up3DeviceWearer,
  val monitoringOrder: Up3MonitoringOrder,
)
