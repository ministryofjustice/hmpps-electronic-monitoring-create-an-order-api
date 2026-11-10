package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.time.Instant
import java.util.UUID

enum class SimulatedCaseState(val caseState: CaseState) {
  PROCESSING(CaseState.NEW),
  ACCEPTED(CaseState.AWAITING_VALIDATION),
}

data class UpdateSimulatedCaseStateDto(
  val state: SimulatedCaseState,
  val expectedCurrentOrderVersionId: UUID,
  val expectedSimulatedSubmissionVersionId: UUID,
)

data class SimulateReturnDto(
  val eventId: UUID,
  val datetimeOfStatusChange: Instant,
  @field:NotEmpty
  @field:Size(max = 50)
  @field:Valid
  val reasons: List<SimulatedReturnReasonDto>,
  val expectedCurrentOrderVersionId: UUID,
  val expectedSimulatedSubmissionVersionId: UUID,
)

data class SimulatedReturnReasonDto(
  @field:NotBlank
  @field:Size(max = 255)
  val section: String,
  @field:Size(max = 10000)
  val details: String? = null,
)

data class Up3SimulationDto(
  val orderId: UUID,
  val currentOrderVersionId: UUID,
  val currentOrderVersionNumber: Int,
  val simulatedSubmissionVersionId: UUID?,
  val simulatedSubmissionVersionNumber: Int?,
  val caseId: String?,
  val caseState: CaseState?,
  val orderStatus: OrderStatus,
  val isOwner: Boolean,
  val canAmend: Boolean,
  val canControlSubmission: Boolean,
  val lastProcessedEventId: UUID?,
  val capturedUserEmails: Int,
  val capturedNoEmails: Int,
)

data class SimulatedReturnEventDto(
  val eventId: UUID,
  val orderId: UUID,
  val targetOrderVersionId: UUID,
  val simulatedSubmissionVersionId: UUID,
  val status: SimulatedReturnStatus,
  val createdAt: Instant,
  val queuedAt: Instant?,
  val completedAt: Instant?,
  val publicationAttempts: Int,
  val processingFailures: Int,
  val nextPublicationAttemptAt: Instant?,
  val failureStage: SimulationFailureStage?,
  val errorCode: String?,
)

data class Up3SimulationAvailabilityDto(val available: Boolean, val code: String)

enum class SimulatedOrderState {
  DRAFT,
  PROCESSING,
  ACCEPTED,
  RETURNED,
}

data class CreateSimulatedOrderDto(
  val order: OrderDto,
  val state: SimulatedOrderState,
  @field:Valid
  @field:Size(max = 50)
  val reasons: List<SimulatedReturnReasonDto> = emptyList(),
  val creationRequestId: UUID,
)

data class CreatedSimulatedOrderDto(
  val creationRequestId: UUID,
  val createdOrderVersionId: UUID,
  val simulation: Up3SimulationDto,
  val requestedState: SimulatedOrderState,
  val returnEventId: UUID?,
)

enum class SimulatedCreationStatus {
  PENDING,
  CREATED,
}

data class SimulatedCreationReceiptDto(
  val creationRequestId: UUID,
  val status: SimulatedCreationStatus,
  val orderId: UUID?,
  val createdOrderVersionId: UUID?,
  val requestedState: SimulatedOrderState,
  val returnEventId: UUID?,
)
