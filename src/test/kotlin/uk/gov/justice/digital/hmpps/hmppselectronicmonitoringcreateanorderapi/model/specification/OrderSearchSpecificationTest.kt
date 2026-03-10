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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.TagFilter
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
  fun `do not filter when no filter is empty`() {
    val orderWithTag = createOrder(tags = "PRISON,YOUTH")
    val orderWithoutTag = createOrder(tags = "PROBATION")
    val orderWithNoTag = createOrder(tags = "")

    orderRepository.saveAll(listOf(orderWithTag, orderWithoutTag, orderWithNoTag))

    val filter = TagFilter()
    val criteria = OrderSearchCriteria(searchTerm = "Joe", filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(3)
  }

  @Test
  fun `can filter by any tag`() {
    val orderWithTag = createOrder(tags = "PRISON,YOUTH")
    val orderWithoutTag = createOrder(tags = "PROBATION")
    val orderWithNoTag = createOrder(tags = "")

    orderRepository.saveAll(listOf(orderWithTag, orderWithoutTag, orderWithNoTag))

    val filter = TagFilter(anyOf = listOf("PRISON"))
    val criteria = OrderSearchCriteria(searchTerm = "Joe", tagFilter = filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(1)
    assertThat(results[0].id).isEqualTo(orderWithTag.id)
  }

  @Test
  fun `match any of the tags in a set`() {
    val orderWithTag = createOrder(tags = "PRISON,YOUTH YCS")
    val orderWithoutTag = createOrder(tags = "Probation")
    val orderWithNoTag = createOrder(tags = "")

    orderRepository.saveAll(listOf(orderWithTag, orderWithoutTag, orderWithNoTag))

    val filter = TagFilter(anyOf = listOf("PRISON", "Probation"))
    val criteria = OrderSearchCriteria(searchTerm = "Joe", tagFilter = filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(2)
  }

  @Test
  fun `match all of the tags in a set`() {
    val orderWithPrisonA = createOrder(tags = "PRISON,PRISON A")
    val orderWithPrisonB = createOrder(tags = "PRISON,PRISON B")
    val orderWithProbation = createOrder(tags = "Probation")

    orderRepository.saveAll(listOf(orderWithPrisonA, orderWithPrisonB, orderWithProbation))

    val filter = TagFilter(allOf = listOf("PRISON", "PRISON A"))
    val criteria = OrderSearchCriteria(searchTerm = "Joe", tagFilter = filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(1)
  }

  @Test
  fun `doesnt match exluded tags`() {
    val orderWithPrison = createOrder(tags = "PRISON")
    val orderWithYouthPrison = createOrder(tags = "PRISON,YOUTH YCS")
    val orderWithProbation = createOrder(tags = "Probation")
    val orderWithNoTags = createOrder(tags = null)

    orderRepository.saveAll(listOf(orderWithPrison, orderWithYouthPrison, orderWithProbation, orderWithNoTags))

    val filter = TagFilter(noneOf = listOf("YOUTH YCS"))
    val criteria = OrderSearchCriteria(searchTerm = "Joe", tagFilter = filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(3)
  }

  @Test
  fun `does not match substring`() {
    val orderWithPrison = createOrder(tags = "PRISON")
    val orderWithExtraPrison = createOrder(tags = "BLAH PRISON BLAH")
    val orderWithProbation = createOrder(tags = "Probation")

    orderRepository.saveAll(listOf(orderWithPrison, orderWithExtraPrison, orderWithProbation))

    val filter = TagFilter(anyOf = listOf("PRISON"))
    val criteria = OrderSearchCriteria(searchTerm = "Joe", tagFilter = filter)
    val spec = OrderSearchSpecification(criteria)

    val results = orderRepository.findAll(spec)

    assertThat(results).hasSize(1)
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
