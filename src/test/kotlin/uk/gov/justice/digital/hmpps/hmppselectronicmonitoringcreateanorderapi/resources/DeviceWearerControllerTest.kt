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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.DeviceWearerController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.DeviceWearerService
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
  private val mockDateOfBirth: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(1970, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private lateinit var authentication: Authentication
  private lateinit var mockOrderId: UUID

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
    mockOrderId = UUID.randomUUID()
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
      interpreterRequired = false,
    )
    UpdateDeviceWearerDto(
      alias = mockAlias,
      firstName = mockFirstName,
      lastName = mockLastName,
      gender = mockGender,
      dateOfBirth = mockDateOfBirth,
      interpreterRequired = false,
    )
    `when`(
      deviceWearerService.updateDeviceWearer(
        username = mockUser,
        orderId = mockOrderId,
        deviceWearerUpdateRecord = UpdateDeviceWearerDto(
          alias = mockAlias,
          firstName = mockFirstName,
          lastName = mockLastName,
          gender = mockGender,
          dateOfBirth = mockDateOfBirth,
          interpreterRequired = false,
        ),
      ),
    ).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.updateDeviceWearer(
      orderId = mockOrderId,
      deviceWearerUpdateRecord = UpdateDeviceWearerDto(
        alias = mockAlias,
        firstName = mockFirstName,
        lastName = mockLastName,
        gender = mockGender,
        dateOfBirth = mockDateOfBirth,
        interpreterRequired = false,
      ),
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
        orderId = mockOrderId,
        username = mockUser,
        deviceWearerUpdateRecord = UpdateDeviceWearerDto(
          alias = mockAlias,
          firstName = mockFirstName,
          lastName = mockLastName,
          gender = mockGender,
          dateOfBirth = mockDateOfBirth,
          interpreterRequired = false,
        ),
      ),
    ).thenReturn(mockDeviceWearer)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.updateDeviceWearer(
      orderId = mockOrderId,
      deviceWearerUpdateRecord = UpdateDeviceWearerDto(
        alias = mockAlias,
        firstName = mockFirstName,
        lastName = mockLastName,
        gender = mockGender,
        dateOfBirth = mockDateOfBirth,
        interpreterRequired = false,
      ),
      authentication = authentication,
    )

    Assertions.assertThat(result.body).isEqualTo(mockDeviceWearer)
    Assertions.assertThat(result.statusCode.is4xxClientError)
  }
}
