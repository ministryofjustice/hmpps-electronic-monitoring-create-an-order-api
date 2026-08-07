package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty

data class Up3RetrieveDWandMO(
  @JsonProperty("case_id")
  val caseId: String,
  @JsonProperty("device_wearer")
  val deviceWearer: Up3DeviceWearer,
  @JsonProperty("monitoring_order")
  val monitoringOrder: Up3MonitoringOrder,
)
