package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import java.util.*

enum class SubmissionStatus {
  NOT_SUBMITTED,
  SUCCESS,
  FAILURE,
}

@Entity
@Table(name = "FMS_DEVICE_WEARER_SUBMISSION_RESULT")
data class FmsDeviceWearerSubmissionResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  val status: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,

  @Column(name = "PAYLOAD", nullable = false, columnDefinition = "varchar(409600)")
  val payload: String? = "",

  @Column(name = "DEVICE_WEARER_ID", nullable = false)
  val deviceWearerId: String? = "",

  @Column(name = "ERROR", nullable = false)
  val error: String? = "",
)

@Entity
@Table(name = "FMS_MONITORING_ORDER_SUBMISSION_RESULT")
data class FmsMonitoringOrderSubmissionResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  val status: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,

  @Column(name = "PAYLOAD", nullable = false, columnDefinition = "varchar(409600)")
  val payload: String? = "",

  @Column(name = "MONITORING_ORDER_ID", nullable = false)
  val monitoringOrderId: String? = "",

  @Column(name = "ERROR", nullable = false)
  val error: String? = "",
)

@Entity
@Table(name = "FMS_ATTACHMENT_SUBMISSION_RESULT")
data class FmsAttachmentSubmissionResult(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  val status: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,

  @Column(name = "SYS_ID", nullable = false)
  val sysId: String? = "",

  @Column(name = "FILE_TYPE", nullable = false)
  val fileType: String? = "",

  @Column(name = "ATTACHMENT_ID", nullable = false)
  val attachmentId: String? = "",

  @Column(name = "ERROR", nullable = false)
  val error: String? = "",
)

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
  var monitoringOrderResult: FmsMonitoringOrderSubmissionResult,

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
        return deviceWearerResult.error!!
      }

      if (monitoringOrderResult.status == SubmissionStatus.FAILURE) {
        return monitoringOrderResult.error!!
      }

      if (attachmentResults.any { it.status == SubmissionStatus.FAILURE }) {
        return attachmentResults.first { it.status == SubmissionStatus.FAILURE }.error!!
      }

      return ""
    }
}
