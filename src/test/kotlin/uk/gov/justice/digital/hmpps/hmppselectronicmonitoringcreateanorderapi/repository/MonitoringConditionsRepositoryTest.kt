package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Pilot
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.*

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:men:cemo-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
  ],
)
class MonitoringConditionsRepositoryTest {
  @Autowired
  lateinit var orderRepo: OrderRepository

  @BeforeEach
  fun saveBasicOrder() {
    orderRepo.save(
      Order(
        id = mockOrderId,
        versions = mutableListOf(
          OrderVersion(
            id = mockVersionId,
            username = "something",
            status = OrderStatus.IN_PROGRESS,
            type = RequestType.REQUEST,
            orderId = mockOrderId,
            dataDictionaryVersion = DataDictionaryVersion.DDV4,
          ),
        ),
      ),
    )
  }

  @AfterEach
  fun tearDown() {
    orderRepo.deleteAll()
  }

  val mockOrderId: UUID = UUID.randomUUID()
  val mockVersionId: UUID = UUID.randomUUID()

  @Nested
  inner class WhenSavingMonitoringConditions {

    @Test
    fun `Can add pilot`() {
      saveBasicOrder()

      val order = orderRepo.findById(mockOrderId).get()

      order.monitoringConditions =
        MonitoringConditions(
          versionId = mockVersionId,
          pilot = Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC,
        )

      val finalMonitoringConditions = orderRepo.save(order).monitoringConditions

      assert(
        finalMonitoringConditions?.pilot == Pilot.DOMESTIC_ABUSE_PERPETRATOR_ON_LICENCE_HOME_DETENTION_CURFEW_DAPOL_HDC,
      )
    }
  }
}
