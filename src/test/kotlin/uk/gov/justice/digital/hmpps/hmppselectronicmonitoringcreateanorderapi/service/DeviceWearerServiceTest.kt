package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import java.util.*

@ActiveProfiles("test")
@JsonTest
class DeviceWearerServiceTest {
  private lateinit var repo: DeviceWearerRepository
  private lateinit var service: DeviceWearerService
  private lateinit var mockOrderId: UUID

  @BeforeEach
  fun setup() {
    repo = mock(DeviceWearerRepository::class.java)
    service = DeviceWearerService(repo)
    mockOrderId = UUID.randomUUID()
  }

  @Test
  fun `Create a new device wearer with order ID and save to database`() {
    val result = service.createDeviceWearer(mockOrderId)

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.orderId).isEqualTo(mockOrderId)
    Assertions.assertThat(result.firstName).isNull()
    Assertions.assertThat(result.lastName).isNull()
    Assertions.assertThat(result.gender).isNull()
    Assertions.assertThat(result.dateOfBirth).isNull()
    argumentCaptor<DeviceWearer>().apply {
      verify(repo, times(1)).save(capture())
      Assertions.assertThat(firstValue).isEqualTo(result)
    }
  }
}
