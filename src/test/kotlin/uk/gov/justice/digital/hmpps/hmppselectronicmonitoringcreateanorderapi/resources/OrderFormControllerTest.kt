package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resources

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.OrderFormController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderFormService

@ActiveProfiles("test")
@JsonTest
class OrderFormControllerTest {
  private val orderFromService: OrderFormService = mock()
  private val controller = OrderFormController(orderFromService)
  private lateinit var authentication: Authentication

  @BeforeEach
  fun setup() {
    authentication = mock(Authentication::class.java)
  }

  @Test
  fun `create a new order form and return`() {
    val mockForm = OrderForm(title = "mockForm", username = "mockUser", status = FormStatus.IN_PROGRESS)
    `when`(orderFromService.createOrderForm("mockForm", "mockUser")).thenReturn(mockForm)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.createForm("mockForm", authentication)
    Assertions.assertThat(result.body).isEqualTo(mockForm)
  }
}
