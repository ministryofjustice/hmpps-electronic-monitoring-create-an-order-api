package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import java.util.*

@Entity
@Table(name = "SUBMIT_FMS_ORDER_RESULT")
data class SubmitFmsOrderResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),
  @Column(name = "FMS_DEVICE_WEARER", nullable = true, columnDefinition = "varchar(409600)")
  var fmsDeviceWearerRequest: String? = "",
  @Column(name = "FMS_DEVICE_WEARER_ID", nullable = true)
  var deviceWearerId: String? = "",
  @Column(name = "FMS_ORDER", nullable = true, columnDefinition = "varchar(409600)")
  var fmsOrderRequest: String? = "",
  @Column(name = "FMS_ORDER_ID", nullable = true)
  var fmsOrderId: String? = "",
  @Column(name = "SUCCESS", nullable = true)
  var success: Boolean = false,
  @Enumerated(EnumType.STRING)
  @Column(name = "FMS_ORDER_SOURCE", nullable = false)
  var orderSource: FmsOrderSource,
  @Column(name = "ERROR", nullable = false)
  var error: String? = "",
)
