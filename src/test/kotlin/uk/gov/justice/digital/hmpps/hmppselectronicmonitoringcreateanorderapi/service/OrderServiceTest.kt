package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.SercoClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as fmsDeviceWearer

@ActiveProfiles("test")
@JsonTest
class OrderServiceTest {
  private lateinit var repo: OrderRepository
  private lateinit var sercoClient: SercoClient
  private lateinit var service: OrderService

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    sercoClient = mock(SercoClient::class.java)
    service = OrderService(repo, sercoClient)
  }

  @Test
  fun `Create a new order for user and save to database`() {
    val result = service.createOrder("mockUser")

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }

  @Test
  fun `Create FMS device wearer and save fms device wearer id against order`() {
    val mockOrder = Order(
      username = "mockUser",
      status = OrderStatus.IN_PROGRESS,
    )
    mockOrder.deviceWearer = DeviceWearer(
      orderId = mockOrder.id,
      adultAtTimeOfInstallation = true, dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
    )
    mockOrder.addresses = mutableListOf(
      Address(
        orderId = mockOrder.id,
        addressLine1 = "20 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",
        addressType = AddressType.PRIMARY,
      ),
    )

    whenever(repo.findByUsernameAndId("mockUser", mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(sercoClient.createDeviceWearer(any<fmsDeviceWearer>(), eq(mockOrder.id))).thenReturn(
      SercoResponse(
        result = listOf(
          SercoResult("", "mockSercoId"),
        ),
      ),
    )

    service.submitOrder(mockOrder.id, "mockUser")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue.fmsDeviceWearerId).isEqualTo("mockSercoId")
    }
  }
}
