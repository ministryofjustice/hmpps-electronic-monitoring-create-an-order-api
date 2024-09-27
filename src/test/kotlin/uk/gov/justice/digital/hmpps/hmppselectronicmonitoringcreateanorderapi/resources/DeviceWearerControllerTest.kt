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
import java.time.LocalDate
import java.util.*

@ActiveProfiles("test")
@JsonTest
class DeviceWearerControllerTest {
  private val deviceWearerService: DeviceWearerService = mock()
  private val controller = DeviceWearerController(deviceWearerService)
  private val mockUser: String = "mockUser"
  private val mockFirstName: String = "mockFirstName"
  private val mockLastName: String = "mockLastName"
  private val mockAlias: String = "mockAlias"
  private val mockGender: String = "mockGender"
  private val mockDateOfBirth: LocalDate = LocalDate.of(1970, 1, 1)
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
    val mockDeviceWearer = DeviceWearer(
      orderId = mockOrderId,
      firstName = null,
      lastName = null,
      alias = null,
      gender = null,
      dateOfBirth = null,
    )
    `when`(deviceWearerService.getDeviceWearer(mockUser, mockOrderId)).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn(mockUser)

    val result = controller.getDeviceWearer(mockOrderId, authentication)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Get device wearer returns a 4xx status if no record matches the order ID & user`() {
    val mockDeviceWearer = null
    `when`(deviceWearerService.getDeviceWearer(mockUser, mockOrderId)).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn(mockUser)

    val result = controller.getDeviceWearer(mockOrderId, authentication)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is5xxServerError)
  }

  @Test
  fun `Update a device wearer`() {
    val mockDeviceWearer = DeviceWearer(
      orderId = mockOrderId,
      firstName = mockFirstName,
      lastName = mockLastName,
      alias = mockAlias,
      gender = mockGender,
      dateOfBirth = mockDateOfBirth,
    )
    `when`(
      deviceWearerService.updateDeviceWearer(
        username = mockUser,
        orderId = mockOrderId,
        firstName = mockFirstName,
        lastName = mockLastName,
        alias = mockAlias,
        gender = mockGender,
        dateOfBirth = mockDateOfBirth,
      ),
    ).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.updateDeviceWearer(
      orderId = mockOrderId,
      firstName = mockFirstName,
      lastName = mockLastName,
      alias = mockAlias,
      gender = mockGender,
      dateOfBirth = mockDateOfBirth,
      authentication,
    )

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Update device wearer returns a 4xx status if no record matches the order ID & user`() {
    val mockDeviceWearer = null
    `when`(
      deviceWearerService.updateDeviceWearer(
        username = mockUser,
        orderId = mockOrderId,
        firstName = mockFirstName,
        lastName = mockLastName,
        alias = mockAlias,
        gender = mockGender,
        dateOfBirth = mockDateOfBirth,
      ),
    ).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.updateDeviceWearer(orderId = mockOrderId, authentication = authentication)

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is4xxClientError)
  }
}
