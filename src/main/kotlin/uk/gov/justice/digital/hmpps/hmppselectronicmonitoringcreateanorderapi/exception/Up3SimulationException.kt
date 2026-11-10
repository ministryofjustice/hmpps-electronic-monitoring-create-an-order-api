package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class Up3SimulationException(
  val status: HttpStatus,
  val errorCode: String,
  override val message: String,
  val expectedCurrentOrderVersionId: UUID? = null,
  val currentOrderVersionId: UUID? = null,
  val expectedSimulatedSubmissionVersionId: UUID? = null,
  val simulatedSubmissionVersionId: UUID? = null,
) : RuntimeException(message)

data class Up3SimulationErrorResponse(
  val status: Int,
  val errorCode: String,
  val userMessage: String,
  val expectedCurrentOrderVersionId: UUID?,
  val currentOrderVersionId: UUID?,
  val expectedSimulatedSubmissionVersionId: UUID?,
  val simulatedSubmissionVersionId: UUID?,
)
