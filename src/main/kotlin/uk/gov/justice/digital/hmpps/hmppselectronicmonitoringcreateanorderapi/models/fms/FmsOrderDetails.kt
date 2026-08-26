package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "FMS_ORDER_DETAILS")
data class FmsOrderDetails(
  @Id
  @Column(name = "CASE_ID", nullable = false, unique = true)
  val caseId: String,
  @Column(name = "DEVICE_WEARER_AS_JSON", nullable = false, columnDefinition = "jsonb")
  val deviceWearerAsJson: String,
  @Column(name = "MONITORING_ORDER_AS_JSON", nullable = false, columnDefinition = "jsonb")
  val monitoringOrderAsJson: String,
)
