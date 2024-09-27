package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.time.LocalDate
import java.util.*

class DeviceWearerControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var orderRepo: OrderFormRepository

  @Autowired
  lateinit var deviceWearerRepo: DeviceWearerRepository

  private lateinit var mockOrderId: UUID
  private val mockUser = "mockUser"
  private val mockFirstName: String = "mockFirstName"
  private val mockLastName: String = "mockLastName"
  private val mockAlias: String = "mockAlias"
  private val mockGender: String = "mockGender"
  private val mockDateOfBirth: LocalDate = LocalDate.of(1970, 1, 1)

  @BeforeEach
  fun setup() {
    deviceWearerRepo.deleteAll()
    orderRepo.deleteAll()
    mockOrderId = UUID.randomUUID()
    val order = OrderForm(id = mockOrderId, username = mockUser, status = FormStatus.IN_PROGRESS)
    orderRepo.save(order)
  }

  @Test
  fun `Create device wearer`() {
    webTestClient.get()
      .uri("/api/CreateDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)

    val deviceWearers = deviceWearerRepo.findAll()
    Assertions.assertThat(deviceWearers).hasSize(1)
    Assertions.assertThat(deviceWearers[0].id).isNotNull()
    Assertions.assertThat(UUID.fromString(deviceWearers[0].id.toString())).isEqualTo(deviceWearers[0].id)
    Assertions.assertThat(deviceWearers[0].orderId).isEqualTo(mockOrderId)
    Assertions.assertThat(deviceWearers[0].firstName).isNull()
    Assertions.assertThat(deviceWearers[0].lastName).isNull()
    Assertions.assertThat(deviceWearers[0].alias).isNull()
    Assertions.assertThat(deviceWearers[0].gender).isNull()
    Assertions.assertThat(deviceWearers[0].dateOfBirth).isNull()
  }

  @Test
  fun `Get a device wearer`() {
    webTestClient.get()
      .uri("/api/CreateDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)

    val getDeviceWearer = webTestClient.get()
      .uri("/api/GetDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(getDeviceWearer.responseBody?.orderId).isEqualTo(mockOrderId)
  }

  @Test
  fun `Get device wearer returns 404 status if a device wearer can't be found`() {
    webTestClient.get()
      .uri("/api/GetDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Get device wearer returns 404 status if the order belongs to another user`() {
    webTestClient.get()
      .uri("/api/CreateDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation("USER_1"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)

    webTestClient.get()
      .uri("/api/GetDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation("USER_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer`() {
    webTestClient.get()
      .uri("/api/CreateDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)

    val updateDeviceWearer = webTestClient.patch()
      .uri("/api/UpdateDeviceWearer?orderId=$mockOrderId&firstName=$mockFirstName&lastName=$mockLastName&alias=$mockAlias&gender=$mockGender&dateOfBirth=$mockDateOfBirth")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(updateDeviceWearer.responseBody?.orderId).isEqualTo(mockOrderId)
    Assertions.assertThat(updateDeviceWearer.responseBody?.firstName).isEqualTo(mockFirstName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.lastName).isEqualTo(mockLastName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.alias).isEqualTo(mockAlias)
    Assertions.assertThat(updateDeviceWearer.responseBody?.gender).isEqualTo(mockGender)
    Assertions.assertThat(updateDeviceWearer.responseBody?.dateOfBirth).isEqualTo(mockDateOfBirth)
  }

  @Test
  fun `Update device wearer returns 404 status if a device wearer can't be found`() {
    webTestClient.patch()
      .uri("/api/UpdateDeviceWearer?orderId=$mockOrderId&firstName=$mockFirstName&lastName=$mockLastName&alias=$mockAlias&gender=$mockGender&dateOfBirth=$mockDateOfBirth")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer returns 404 status if the order belongs to another user`() {
    webTestClient.get()
      .uri("/api/CreateDeviceWearer?orderId=$mockOrderId")
      .headers(setAuthorisation(mockUser))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)

    webTestClient.patch()
      .uri("/api/UpdateDeviceWearer?orderId=$mockOrderId&firstName=$mockFirstName&lastName=$mockLastName&alias=$mockAlias&gender=$mockGender&dateOfBirth=$mockDateOfBirth")
      .headers(setAuthorisation("USER_1"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }
}
