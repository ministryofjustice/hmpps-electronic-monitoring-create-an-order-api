package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import jakarta.persistence.Column
import jakarta.persistence.Id

data class FmsOrderDetails(
  @Id
  @Column(name = "CASE_ID", nullable = false, unique = true)
  val caseId: String,
  @Column(name = "DEVICE_WEARER_AS_JSON", nullable = false, columnDefinition = "varchar(409600)")
  val deviceWearerAsJson: String ,
  @Column(name = "MONITORING_ORDER_AS_JSON", nullable = false, columnDefinition = "varchar(409600)")
  val monitoringOrderAsJson: String ,
  )