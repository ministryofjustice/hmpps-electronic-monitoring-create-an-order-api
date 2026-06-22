package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDapoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
class DapoServiceTest {
  lateinit var repo: OrderRepository

  lateinit var service: DapoService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"
  lateinit var mockOrder: Order

  @BeforeEach
  fun setup() {
    mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          versionId = 0,
          status = OrderStatus.IN_PROGRESS,
          orderId = mockOrderId,
          type = RequestType.REQUEST,
          username = mockUsername,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      ),
    )
    repo = Mockito.mock(OrderRepository::class.java)
    service = DapoService()
    service.orderRepo = repo
  }

  @Test
  fun `should update the order with the new dapo data`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val dto = UpdateDapoDto(clause = "some clause", date = ZonedDateTime.now())
    val result = service.addDapo(
      mockOrderId,
      mockUsername,
      dto,
    )

    assertThat(result).isNotNull
    assertThat(result.clause).isEqualTo(dto.clause)
    assertThat(result.date).isEqualTo(dto.date)
  }

  @Test
  fun `should be able to update existing dapo`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val addDto = UpdateDapoDto(clause = "some clause", date = ZonedDateTime.now())
    val addResult = service.addDapo(
      mockOrderId,
      mockUsername,
      addDto,
    )

    val updateDto = UpdateDapoDto(id = addResult.id, clause = "another clause", date = ZonedDateTime.now())
    val updateResult = service.addDapo(
      mockOrderId,
      mockUsername,
      updateDto,
    )

    assertThat(mockOrder.dapoClauses.size).isEqualTo(1)
    assertThat(updateResult).isNotNull
    assertThat(updateResult.id).isEqualTo(updateDto.id)
    assertThat(updateResult.clause).isEqualTo(updateDto.clause)
    assertThat(updateResult.date).isEqualTo(updateDto.date)
  }

  @Test
  fun `can delete dapo`() {
    mockOrder.dapoClauses.add(Dapo(versionId = mockOrder.versionId, clause = "some clause", date = ZonedDateTime.now()))
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    service.deleteDapo(
      mockOrderId,
      mockUsername,
      mockOrder.dapoClauses[0].id,
    )

    assertThat(mockOrder.dapoClauses).isEmpty()
  }
}
