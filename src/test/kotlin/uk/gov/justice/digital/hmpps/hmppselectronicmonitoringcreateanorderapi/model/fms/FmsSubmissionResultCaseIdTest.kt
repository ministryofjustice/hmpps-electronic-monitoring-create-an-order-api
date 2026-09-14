package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import java.util.UUID

@DisplayName("FmsSubmissionResult.caseId")
class FmsSubmissionResultCaseIdTest {

  private fun buildResult(deviceWearerId: String) = FmsSubmissionResult(
    orderId = UUID.randomUUID(),
    strategy = FmsSubmissionStrategyKind.ORDER,
    orderSource = FmsOrderSource.CEMO,
    deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = deviceWearerId),
  )

  @Test
  @DisplayName("is the device wearer id from the FMS device wearer result")
  fun `is the device wearer id from the fms device wearer result`() {
    val result = buildResult("CASE123")

    assertThat(result.caseId).isEqualTo("CASE123")
  }

  @Test
  @DisplayName("is null when the FMS device wearer result has a blank device wearer id")
  fun `is null when the fms device wearer result has a blank device wearer id`() {
    val result = buildResult("")

    assertThat(result.caseId).isNull()
  }
}
