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
import java.util.*

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
    val mockForm = OrderForm(username = "mockUser", status = FormStatus.IN_PROGRESS)
    `when`(orderFromService.createOrderForm("mockUser")).thenReturn(mockForm)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.createForm(authentication)
    Assertions.assertThat(result.body).isNotNull
    Assertions.assertThat(result.body!!.username).isEqualTo("mockUser")
    Assertions.assertThat(result.body!!.status).isEqualTo(FormStatus.IN_PROGRESS)
    Assertions.assertThat(result.body!!.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.body!!.id.toString())).isEqualTo(result.body!!.id)
  }

  @Test
  fun `query forms for current user and return`() {
    val orderForms: List<OrderForm> = listOf(
      OrderForm(username = "mockUser", status = FormStatus.IN_PROGRESS),
      OrderForm(username = "mockUser", status = FormStatus.IN_PROGRESS),
    )

    `when`(orderFromService.listOrderFormsForUser("mockUser")).thenReturn(orderForms)
    `when`(authentication.name).thenReturn("mockUser")

    val result = controller.listForms(authentication)
    Assertions.assertThat(result.body).isEqualTo(orderForms)
  }
}
