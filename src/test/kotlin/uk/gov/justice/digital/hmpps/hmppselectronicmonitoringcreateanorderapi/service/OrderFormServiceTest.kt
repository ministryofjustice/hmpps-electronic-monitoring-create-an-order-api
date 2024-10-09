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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as fmsDeviceWearer

@ActiveProfiles("test")
@JsonTest
class OrderFormServiceTest {
  private lateinit var repo: OrderFormRepository
  private lateinit var sercoClient: SercoClient
  private lateinit var service: OrderFormService

  @BeforeEach
  fun setup() {
    repo = mock(OrderFormRepository::class.java)
    sercoClient = mock(SercoClient::class.java)
    service = OrderFormService(repo, sercoClient)
  }

  @Test
  fun `Create a new order form with tile and username and save to database`() {
    val result = service.createOrderForm("mockUser")

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(FormStatus.IN_PROGRESS)
    argumentCaptor<OrderForm>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }

  @Test
  fun `Create FMS device wearer and save fms device wearer id against order`() {
    val mockOrder = OrderForm(
      username = "mockUser",
      status = FormStatus.IN_PROGRESS,
    )
    mockOrder.deviceWearer = DeviceWearer(
      orderId = mockOrder.id,
      adultAtTimeOfInstallation = true, dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
    )
    mockOrder.deviceWearerAddresses = mutableListOf(
      DeviceWearerAddress(
        orderId = mockOrder.id,
        addressLine1 = "20 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",
        addressType = DeviceWearerAddressType.PRIMARY,
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

    service.submitOrderForm(mockOrder.id, "mockUser")

    argumentCaptor<OrderForm>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue.fmsDeviceWearerId).isEqualTo("mockSercoId")
    }
  }
}
