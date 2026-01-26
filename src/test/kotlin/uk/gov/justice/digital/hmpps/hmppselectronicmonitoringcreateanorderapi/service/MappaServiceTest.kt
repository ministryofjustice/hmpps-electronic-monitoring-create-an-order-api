import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MappaService
import java.util.Optional
import java.util.UUID

@ActiveProfiles("test")
class MappaServiceTest {
  private val orderRepo: OrderRepository = mock()
  private lateinit var service: MappaService

  private lateinit var mockOrder: Order
  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "mockUsername"

  @BeforeEach
  fun setup() {
    service = MappaService()
    service.orderRepo = orderRepo

    mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          orderId = mockOrderId,
          username = mockUsername,
          status = OrderStatus.IN_PROGRESS,
          type = RequestType.REQUEST,
          dataDictionaryVersion = DataDictionaryVersion.DDV6,
        ),
      ),
    )
  }

  @Test
  fun `should be able to update mappa values`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateDto = UpdateMappaDto(
      level = MappaLevel.MAPPA_ONE,
      category = MappaCategory.CATEGORY_ONE,
    )

    val result = service.updateMappa(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.mappa?.level).isEqualTo(MappaLevel.MAPPA_ONE)
    assertThat(mockOrder.mappa?.category).isEqualTo(MappaCategory.CATEGORY_ONE)
    assertThat(result.level).isEqualTo(MappaLevel.MAPPA_ONE)
    assertThat(result.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `mappa level is optional`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateDto = UpdateMappaDto(
      category = MappaCategory.CATEGORY_ONE,
    )

    service.updateMappa(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.mappa?.level).isNull()
    assertThat(mockOrder.mappa?.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `mappa category is optional`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateDto = UpdateMappaDto(
      level = MappaLevel.MAPPA_ONE,
    )

    service.updateMappa(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.mappa?.category).isNull()
    assertThat(mockOrder.mappa?.level).isEqualTo(MappaLevel.MAPPA_ONE)
  }
}
