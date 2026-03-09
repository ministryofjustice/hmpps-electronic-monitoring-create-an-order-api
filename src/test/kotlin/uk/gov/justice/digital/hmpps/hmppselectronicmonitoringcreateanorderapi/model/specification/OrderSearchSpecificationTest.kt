package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.specification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSearchSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.UUID

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:cemo-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
  ],
)
@ActiveProfiles("test")
class OrderSearchSpecificationTest {

  @Autowired
  lateinit var orderRepository: OrderRepository

  @BeforeEach
  fun setup() {
    orderRepository.deleteAll()
  }

  @Test
  fun `should filter orders by tags`() {
    val orderWithTag = createOrder(tags = "PRISON,YOUTH")
    val orderWithoutTag = createOrder(tags = "PROBATION")
    val orderWithNoTag = createOrder(tags = "")

    orderRepository.saveAll(listOf(orderWithTag, orderWithoutTag, orderWithNoTag))

    val criteria = OrderSearchCriteria(searchTerm = "Joe", tags = listOf("PRISON"))
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(1)
    assertThat(results[0].id).isEqualTo(orderWithTag.id)
  }

  private fun createOrder(tags: String?): Order {
    val orderId = UUID.randomUUID()
    val versionId = UUID.randomUUID()
    val order = Order(id = orderId)
    val version = OrderVersion(
      id = versionId,
      orderId = orderId,
      versionId = 1,
      username = "test-user",
      status = OrderStatus.SUBMITTED,
      dataDictionaryVersion = DataDictionaryVersion.DDV6,
      type = RequestType.REQUEST,
      tags = tags,
    )
    val deviceWearer = DeviceWearer(
      versionId = versionId,
      firstName = "Joe",
      lastName = "Bloggs",
    )
    version.deviceWearer = deviceWearer
    order.versions = mutableListOf(version)

    return order
  }
}
