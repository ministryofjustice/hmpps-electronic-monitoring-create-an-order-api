package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.MonitoringConditionsController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsService
import java.util.*

class MonitoringConditionsControllerTest {
  private val monitoringConditionsService: MonitoringConditionsService = mock()
  private val controller = MonitoringConditionsController(monitoringConditionsService)
  private val mockUsername = "mockUser"

  private lateinit var mockOrderId: UUID
  private lateinit var mockVersionId: UUID
  private lateinit var authentication: Authentication

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
    mockOrderId = UUID.randomUUID()
    mockVersionId = UUID.randomUUID()
  }

  @Test
  fun `Update monitoring conditions pilot`() {
    val mockMonitoringConditions = MonitoringConditions(versionId = mockVersionId)

    `when`(
      monitoringConditionsService.updateMonitoringConditions(
        orderId = mockOrderId,
        username = mockUsername,
        updateRecord = UpdateMonitoringConditionsDto(pilot = "some pilot"),
      ),
    ).thenReturn(mockMonitoringConditions)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.updateMonitoringConditions(
      orderId = mockOrderId,
      monitoringConditionsUpdateRecord = UpdateMonitoringConditionsDto(pilot = "some pilot"),
      authentication = authentication,
    )

    assertThat(result.statusCode.is2xxSuccessful).isEqualTo(true)
    assertThat(result.body).isEqualTo(mockMonitoringConditions)
  }
}