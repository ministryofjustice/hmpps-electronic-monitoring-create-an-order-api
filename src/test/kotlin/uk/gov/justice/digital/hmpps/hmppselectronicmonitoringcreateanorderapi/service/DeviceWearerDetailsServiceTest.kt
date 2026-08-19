package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.PrisonerDetailsApi

class DeviceWearerDetailsServiceTest {
  class TestClient : PrisonerDetailsApi {
    override fun getPersionDetails(prisonNumber: String) {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun `returns success message when device wearer details are found`() {
    val service = DeviceWearerDetailsService(TestClient())

    val prisonNumber = "1234"

    val response = service.storeDetails(prisonNumber)

    assertThat(response.success).isEqualTo(true)
  }
}
