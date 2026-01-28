package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateOffenceAdditionalDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@ActiveProfiles("test")
class OffenceAdditionalDetailsServiceTest {
  lateinit var repo: OrderRepository

  lateinit var service: OffenceAdditionalDetailsService

  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "username"

  private val mockOrder = Order(
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

  @BeforeEach
  fun setup() {
    repo = Mockito.mock(OrderRepository::class.java)
    service = OffenceAdditionalDetailsService()
    service.orderRepo = repo
  }

  @Test
  fun `should update order with offence details`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val dto = UpdateOffenceAdditionalDetailsDto(
      additionalDetailsRequired = true,
      additionalDetails = "initial offence additional details",
    )
    val result = service.updateOffenceAdditionalDetails(mockOrderId, mockUsername, dto)

    assertThat(result).isNotNull
    assertThat(result.additionalDetails).isEqualTo(dto.additionalDetails)
  }

  @Test
  fun `should update order with updated offence details`() {
    whenever(repo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(repo.save(mockOrder)).thenReturn(mockOrder)

    val addDto = UpdateOffenceAdditionalDetailsDto(
      additionalDetailsRequired = true,
      additionalDetails = "initial offence additional details",
    )
    val addResult = service.updateOffenceAdditionalDetails(mockOrderId, mockUsername, addDto)

    val updateDto = UpdateOffenceAdditionalDetailsDto(
      id = addResult.id,
      additionalDetailsRequired = true,
      "new details",
    )
    val updateResult = service.updateOffenceAdditionalDetails(mockOrderId, mockUsername, updateDto)

    assertThat(updateResult).isNotNull
    assertThat(updateResult.id).isEqualTo(addResult.id)
    assertThat(updateResult.additionalDetails).isEqualTo("new details")
    assertThat(mockOrder.offenceAdditionalDetails?.additionalDetails).isEqualTo("new details")
  }
}
