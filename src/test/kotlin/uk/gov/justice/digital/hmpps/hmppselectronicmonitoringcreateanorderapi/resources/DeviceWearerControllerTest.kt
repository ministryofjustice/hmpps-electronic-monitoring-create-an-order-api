package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resources

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.DeviceWearerController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
import java.util.*

@ActiveProfiles("test")
@JsonTest
class DeviceWearerControllerTest {
  private val deviceWearerService: DeviceWearerService = mock()
  private val controller = DeviceWearerController(deviceWearerService)
  private lateinit var authentication: Authentication
  private lateinit var mockOrderId: UUID

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
    mockOrderId = UUID.randomUUID()
  }

  @Test
  fun `create a new device wearer and return`() {
    val mockDeviceWearer = DeviceWearer(orderId = mockOrderId)
    `when`(deviceWearerService.createDeviceWearer(mockOrderId)).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.createDeviceWearer(mockOrderId)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Get a device wearer`() {
    val mockDeviceWearer = DeviceWearer(orderId = mockOrderId)
    `when`(deviceWearerService.getDeviceWearer(mockOrderId)).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.getDeviceWearer(mockOrderId)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Return a 4xx status if no device wearer is found with the specified order ID`() {
    val mockDeviceWearer = null
    `when`(deviceWearerService.getDeviceWearer(mockOrderId)).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.getDeviceWearer(mockOrderId)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is4xxClientError)
  }
}
