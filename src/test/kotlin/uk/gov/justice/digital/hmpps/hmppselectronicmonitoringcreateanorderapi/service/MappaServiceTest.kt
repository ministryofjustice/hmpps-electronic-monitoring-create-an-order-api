import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateIsMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMappaDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
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

  @Test
  fun `can create new order parameters and update isMappa`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockDto = UpdateIsMappaDto(
      isMappa = YesNoUnknown.YES,
    )

    service.updateIsMappa(mockOrderId, mockUsername, mockDto)

    assertThat(mockOrder.mappa?.isMappa).isEqualTo(YesNoUnknown.YES)
  }

  @Test
  fun `can update existing mappa`() {
    val paramId = UUID.randomUUID()
    mockOrder.mappa = Mappa(id = paramId, versionId = mockVersionId, isMappa = YesNoUnknown.YES)
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockDto = UpdateIsMappaDto(
      isMappa = YesNoUnknown.NO,
    )

    service.updateIsMappa(mockOrderId, mockUsername, mockDto)

    assertThat(mockOrder.mappa?.id).isEqualTo(paramId)
    assertThat(mockOrder.mappa?.isMappa).isEqualTo(YesNoUnknown.NO)
  }

  @Test
  fun `removes existing mappa answers if no`() {
    val paramId = UUID.randomUUID()
    mockOrder.mappa = Mappa(
      id = paramId,
      versionId = mockVersionId,
      isMappa = YesNoUnknown.YES,
      level = MappaLevel.MAPPA_ONE,
      category = MappaCategory.CATEGORY_ONE,
    )
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockDto = UpdateIsMappaDto(
      isMappa = YesNoUnknown.NO,
    )

    service.updateIsMappa(mockOrderId, mockUsername, mockDto)

    assertThat(mockOrder.mappa?.id).isEqualTo(paramId)
    assertThat(mockOrder.mappa?.isMappa).isEqualTo(YesNoUnknown.NO)
    assertThat(mockOrder.mappa?.level).isNull()
    assertThat(mockOrder.mappa?.category).isNull()
  }

  @Test
  fun `removes existing mappa answers if unknown`() {
    val paramId = UUID.randomUUID()
    mockOrder.mappa = Mappa(
      id = paramId,
      versionId = mockVersionId,
      isMappa = YesNoUnknown.YES,
      level = MappaLevel.MAPPA_ONE,
      category = MappaCategory.CATEGORY_ONE,
    )
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockDto = UpdateIsMappaDto(
      isMappa = YesNoUnknown.UNKNOWN,
    )

    service.updateIsMappa(mockOrderId, mockUsername, mockDto)

    assertThat(mockOrder.mappa?.id).isEqualTo(paramId)
    assertThat(mockOrder.mappa?.isMappa).isEqualTo(YesNoUnknown.UNKNOWN)
    assertThat(mockOrder.mappa?.level).isNull()
    assertThat(mockOrder.mappa?.category).isNull()
  }
}
