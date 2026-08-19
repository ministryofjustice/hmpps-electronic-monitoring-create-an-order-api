package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.PrisonerDetails
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DeviceWearerDetailsServiceTest {
  val baseDetails = PrisonerDetails(
    firstName = null,
    middleNames = null,
    lastName = null,
    dateOfBirth = null,
    sex = null,
    identifiers = null,
  )

  class TestClient : PrisonerDetailsApi {
    var prisonDetailsResponse = PrisonerDetails(
      firstName = null,
      middleNames = null,
      lastName = null,
      dateOfBirth = null,
      sex = null,
      identifiers = null,
    )

    fun setMockResponse(value: PrisonerDetails) {
      prisonDetailsResponse = value
    }

    override fun getPrisonerDetails(prisonNumber: String): PrisonerDetails = prisonDetailsResponse
  }

  var client: TestClient = TestClient()

  @BeforeEach
  fun setup() {
    client = TestClient()
  }

  @Test
  fun `get details returns bob as first name when set to bob`() {
    client.setMockResponse(baseDetails.copy(firstName = "Bob"))

    val service = DeviceWearerDetailsService(client)

    val res = service.getDetailsOverview("1234")

    assertThat(res.firstName).isEqualTo("Bob")
  }

  @Test
  fun `get details returns cat as first name when set to cat`() {
    client.setMockResponse(baseDetails.copy(firstName = "Cat"))

    val service = DeviceWearerDetailsService(client)

    val res = service.getDetailsOverview("1234")

    assertThat(res.firstName).isEqualTo("Cat")
  }

  @Test
  fun `get details returns basic prison details`() {
    client.setMockResponse(baseDetails.copy(firstName = "Bob", lastName = "Builder", dateOfBirth = "1990-08-21"))

    val service = DeviceWearerDetailsService(client)

    val res = service.getDetailsOverview("1234")

    assertThat(res.firstName).isEqualTo("Bob")
    assertThat(res.lastName).isEqualTo("Builder")
    assertThat(res.dateOfBirth).isEqualTo(
      ZonedDateTime.of(
        LocalDateTime.of(1990, 8, 21, 0, 0),
        ZoneId.of("Europe/London"),
      ),
    )
    assertThat(res.firstName).isEqualTo("Bob")
  }

  @Test
  fun `returns success message`() {
    val service = DeviceWearerDetailsService(client)

    val response = service.storeDetails("1234")

    assertThat(response.success).isEqualTo(true)
  }
}
