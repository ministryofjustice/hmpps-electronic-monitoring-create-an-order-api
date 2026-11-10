package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.Up3SimulationException
import java.util.UUID

class Up3SimulationErrorContractTest {
  private val handler = HmppsElectronicMonitoringCreateAnOrderApiExceptionHandler(true)
  private val request = MockHttpServletRequest("GET", "/api/dev/up3-simulation/orders/${UUID.randomUUID()}")

  @Test
  fun `unavailable persistence has a stable code without exposing its underlying message`() {
    val result = handler.handleDataAccessException(DataAccessResourceFailureException("database details"), request)
    assertThat(result.statusCode).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
    assertThat(result.body?.errorCode).isEqualTo("UP3_SIMULATION_UNAVAILABLE")
    assertThat(result.body?.userMessage).doesNotContain("database details")
  }

  @Test
  fun `concurrent identity constraint failures are conflicts not unavailable`() {
    val result = handler.handleDataAccessException(DataIntegrityViolationException("constraint details"), request)
    assertThat(result.statusCode).isEqualTo(HttpStatus.CONFLICT)
    assertThat(result.body?.errorCode).isEqualTo("UP3_SIMULATION_ID_CONFLICT")
  }

  @Test
  fun `non simulator persistence failures retain the existing generic response`() {
    val result = handler.handleDataAccessException(
      DataAccessResourceFailureException("database details"),
      MockHttpServletRequest("GET", "/api/orders"),
    )
    assertThat(result.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(result.body?.userMessage).isEqualTo("500 Internal Server Error")
    assertThat(result.body?.errorCode).isNull()
  }

  @Test
  fun `stale conflicts distinguish expected and actual version identities`() {
    val expectedCurrent = UUID.randomUUID()
    val actualCurrent = UUID.randomUUID()
    val submission = UUID.randomUUID()
    val result = handler.handleUp3SimulationException(
      Up3SimulationException(
        HttpStatus.CONFLICT,
        "UP3_SIMULATION_STALE_VERSION",
        "Refresh",
        expectedCurrent,
        actualCurrent,
        submission,
        submission,
      ),
    )
    assertThat(result.statusCode).isEqualTo(HttpStatus.CONFLICT)
    assertThat(result.body?.expectedCurrentOrderVersionId).isEqualTo(expectedCurrent)
    assertThat(result.body?.currentOrderVersionId).isEqualTo(actualCurrent)
    assertThat(result.body?.expectedSimulatedSubmissionVersionId).isEqualTo(submission)
    assertThat(result.body?.simulatedSubmissionVersionId).isEqualTo(submission)
  }
}
