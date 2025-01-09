package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import java.util.*

@Entity
@Table(name = "FMS_SUBMISSION_RESULT")
data class FmsSubmissionResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false)
  val orderId: UUID,

  @Enumerated(EnumType.STRING)
  @Column(name = "SUBMISSION_STRATEGY", nullable = false)
  val strategy: FmsSubmissionStrategyKind,

  @Enumerated(EnumType.STRING)
  @Column(name = "FMS_ORDER_SOURCE", nullable = false)
  var orderSource: FmsOrderSource,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], orphanRemoval = true)
  @JoinColumn(name = "fms_device_wearer_result_id", referencedColumnName = "id")
  var deviceWearerResult: FmsDeviceWearerSubmissionResult,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], orphanRemoval = true)
  @JoinColumn(name = "fms_monitoring_order_result_id", referencedColumnName = "id")
  var monitoringOrderResult: FmsMonitoringOrderSubmissionResult = FmsMonitoringOrderSubmissionResult(),

  @OneToMany(fetch = FetchType.EAGER, cascade = [ALL], orphanRemoval = true)
  @JoinColumn(name = "fms_attachment_result_id", referencedColumnName = "id")
  var attachmentResults: MutableList<FmsAttachmentSubmissionResult> = mutableListOf(),
) {
  val success: Boolean
    get() {
      val deviceWearerSuccess = deviceWearerResult.status == SubmissionStatus.SUCCESS
      val monitoringOrderSuccess = monitoringOrderResult.status == SubmissionStatus.SUCCESS
      val attachmentsSuccess = attachmentResults.all { it.status == SubmissionStatus.SUCCESS }

      return deviceWearerSuccess && monitoringOrderSuccess && attachmentsSuccess
    }

  val error: String
    get() {
      if (deviceWearerResult.status == SubmissionStatus.FAILURE) {
        return deviceWearerResult.error
      }

      if (monitoringOrderResult.status == SubmissionStatus.FAILURE) {
        return monitoringOrderResult.error
      }

      if (attachmentResults.any { it.status == SubmissionStatus.FAILURE }) {
        return attachmentResults.first { it.status == SubmissionStatus.FAILURE }.error
      }

      return ""
    }
}
