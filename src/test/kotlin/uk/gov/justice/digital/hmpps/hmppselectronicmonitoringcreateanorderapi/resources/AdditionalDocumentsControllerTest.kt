package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resources

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderParameters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateHavePhotoDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.AdditionalDocumentsController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.AdditionalDocumentService
import java.util.*

@ActiveProfiles("test")
class AdditionalDocumentsControllerTest {
  private val service: AdditionalDocumentService = mock()
  private val controller = AdditionalDocumentsController(service)
  private val mockUser: String = "mockUser"
  private lateinit var authentication: Authentication
  private lateinit var mockOrderId: UUID

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
    mockOrderId = UUID.randomUUID()
  }

  @Test
  fun `update have photo boolean`() {
    `when`(authentication.name).thenReturn(mockUser)

    val input = UpdateHavePhotoDto(havePhoto = true)
    val mockOrderParameters = OrderParameters(versionId = UUID.randomUUID(), havePhoto = true)
    `when`(
      service.updateHavePhoto(
        orderId = mockOrderId,
        username = mockUser,
        updateRecord = input,
      ),
    ).thenReturn(mockOrderParameters)

    val result = controller.havePhoto(mockOrderId, input, authentication)

    Assertions.assertThat(result.statusCode.is2xxSuccessful)
    Assertions.assertThat(result.body).isEqualTo(mockOrderParameters)
  }
}
