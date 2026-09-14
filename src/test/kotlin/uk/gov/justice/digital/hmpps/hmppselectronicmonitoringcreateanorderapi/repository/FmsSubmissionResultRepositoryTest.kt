package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import java.util.*

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:cemo-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
  ],
)
@DisplayName("FmsSubmissionResultRepository")
class FmsSubmissionResultRepositoryTest {
  @Autowired
  lateinit var repository: FmsSubmissionResultRepository

  @Test
  @DisplayName("finds the submission result by device wearer id when it exists")
  fun `finds the submission result by device wearer id when it exists`() {
    val orderId = UUID.randomUUID()

    repository.save(
      FmsSubmissionResult(
        orderId = orderId,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(
          status = SubmissionStatus.SUCCESS,
          deviceWearerId = "CASE123",
        ),
      ),
    )

    val result = repository.findByCaseId("CASE123")

    assertThat(result).isNotNull
    assertThat(result?.orderId).isEqualTo(orderId)
  }

  @Test
  @DisplayName("returns null when no submission result matches the device wearer id")
  fun `returns null when no submission result matches the device wearer id`() {
    val result = repository.findByCaseId("UNKNOWN_CASE_ID")

    assertThat(result).isNull()
  }
}
