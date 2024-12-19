package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import java.util.*

@Entity
@Table(name = "SUBMIT_FMS_ORDER_RESULT")
data class SubmitFmsOrderResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  @Column(name = "SUBMISSION_STRATEGY", nullable = false)
  val strategy: FmsSubmissionStrategyKind,

  @Column(name = "FMS_DEVICE_WEARER", nullable = true, columnDefinition = "varchar(409600)")
  var fmsDeviceWearerRequest: String? = "",

  @Column(name = "FMS_DEVICE_WEARER_ID", nullable = true)
  var deviceWearerId: String? = "",

  @Column(name = "FMS_ORDER", nullable = true, columnDefinition = "varchar(409600)")
  var fmsOrderRequest: String? = "",
  // TODO: Remove this orderID field. There is no distinct order ID from Serco there will just be a device wearer ID, and the order is stored against that device wearer ID.
  @Column(name = "FMS_ORDER_ID", nullable = true)
  var fmsOrderId: String? = "",

  // FMS_ADDITIONAL_DOCUMENT field. (Will become FMS_ADDITIONAL_DOCUMENTS)
  //  Q: We expect one order and one device wearer, but there can be multiple attachments (device wearer ID, license, maps). Should these be in a different table? Or multiple columns in this table?
  // A: we can save them all in one column in this table. We can save the attachments as a stringified map of [an identifie]against the relevant values from the Serco response (an ID or an address for example). Save 'attachments' as a serialised JSON of these stringified maps.
  // Q: Submit all attachments to Serco then update this field only once? OR perform sequential updates? Probably the former.
  @Column(name = "FMS_ADDITIONAL_DOCUMENT", nullable = true, columnDefinition = "varchar(409600)")
  var fmsAdditionalDocument: String? = "",

  @Column(name = "SUCCESS", nullable = true)
  var success: Boolean = false,

  @Enumerated(EnumType.STRING)
  @Column(name = "FMS_ORDER_SOURCE", nullable = false)
  var orderSource: FmsOrderSource,

  @Column(name = "ERROR", nullable = false)
  var error: String? = "",
)
