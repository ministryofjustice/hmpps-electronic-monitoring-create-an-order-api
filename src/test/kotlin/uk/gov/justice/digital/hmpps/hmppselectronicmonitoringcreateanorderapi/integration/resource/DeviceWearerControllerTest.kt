package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.UpdateDeviceWearerDto
import java.time.LocalDate
import java.util.*

class DeviceWearerControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var deviceWearerRepo: DeviceWearerRepository

  private val mockFirstName: String = "mockFirstName"
  private val mockLastName: String = "mockLastName"
  private val mockAlias: String = "mockAlias"
  private val mockGender: String = "mockGender"
  private val mockDateOfBirth: LocalDate = LocalDate.of(1970, 1, 1)

  @BeforeEach
  fun setup() {
    deviceWearerRepo.deleteAll()
  }

  @Test
  fun `Get a device wearer`() {
    val order = createOrder()

    val getDeviceWearer = webTestClient.get()
      .uri("/api/order/${order.id}/device-wearer")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(getDeviceWearer.responseBody?.orderId).isEqualTo(order.id)
  }

  @Test
  fun `Get device wearer returns 404 status if a device wearer can't be found`() {
    webTestClient.get()
      .uri("/api/order/${UUID.randomUUID()}/device-wearer")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Get device wearer returns 404 status if the order belongs to another user`() {
    val order = createOrder()

    webTestClient.get()
      .uri("/api/order/${order.id}/device-wearer")
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer`() {
    val order = createOrder()
    val updateDeviceWearer = webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer")
      .bodyValue(
        UpdateDeviceWearerDto(
          firstName = mockFirstName,
          lastName = mockLastName,
          alias = mockAlias,
          gender = mockGender,
          dateOfBirth = mockDateOfBirth,
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(updateDeviceWearer.responseBody?.orderId).isEqualTo(order.id)
    Assertions.assertThat(updateDeviceWearer.responseBody?.firstName).isEqualTo(mockFirstName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.lastName).isEqualTo(mockLastName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.alias).isEqualTo(mockAlias)
    Assertions.assertThat(updateDeviceWearer.responseBody?.gender).isEqualTo(mockGender)
    Assertions.assertThat(updateDeviceWearer.responseBody?.dateOfBirth).isEqualTo(mockDateOfBirth)
  }

  @Test
  fun `Update device wearer returns 404 status if a device wearer can't be found`() {
    webTestClient.post()
      .uri("/api/order/${UUID.randomUUID()}/device-wearer")
      .bodyValue(
        UpdateDeviceWearerDto(
          firstName = mockFirstName,
          lastName = mockLastName,
          alias = mockAlias,
          gender = mockGender,
          dateOfBirth = mockDateOfBirth,
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer returns 404 status if the order belongs to another user`() {
    val order = createOrder()
    webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer")
      .bodyValue(
        UpdateDeviceWearerDto(
          firstName = mockFirstName,
          lastName = mockLastName,
          alias = mockAlias,
          gender = mockGender,
          dateOfBirth = mockDateOfBirth,
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }
}
