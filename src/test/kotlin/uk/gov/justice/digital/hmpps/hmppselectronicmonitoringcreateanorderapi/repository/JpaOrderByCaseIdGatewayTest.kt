package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:cemo-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
  ],
)
@Import(JpaOrderByCaseIdGateway::class)
class JpaOrderByCaseIdGatewayTest {
  @Autowired
  lateinit var orderRepository: OrderRepository

  @Autowired
  lateinit var fmsSubmissionResultRepository: FmsSubmissionResultRepository

  @Autowired
  lateinit var gateway: JpaOrderByCaseIdGateway

  @Test
  fun `returns the order when a submission result matches the given case id`() {
    val order = TestUtilities.createReadyToSubmitOrder()
    orderRepository.save(order)

    fmsSubmissionResultRepository.save(
      FmsSubmissionResult(
        orderId = order.id,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = "CASE123"),
      ),
    )

    val result = gateway.findOrderByCaseId("CASE123")

    assertThat(result).isNotNull
    assertThat(result?.id).isEqualTo(order.id)
  }

  @Test
  fun `returns null when no submission result matches the given case id`() {
    val result = gateway.findOrderByCaseId("UNKNOWN_CASE_ID")

    assertThat(result).isNull()
  }
}
