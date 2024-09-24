package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import java.util.*

class OrderFormControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderFormRepository

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Form created and saved in database`() {
    val result = webTestClient.get()
      .uri("/api/CreateForm")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    val forms = repo.findAll()
    Assertions.assertThat(forms).hasSize(1)
    Assertions.assertThat(forms[0].username).isEqualTo("AUTH_ADM")
    Assertions.assertThat(forms[0].status).isEqualTo(FormStatus.IN_PROGRESS)
    Assertions.assertThat(forms[0].id).isNotNull()
    Assertions.assertThat(UUID.fromString(forms[0].id.toString())).isEqualTo(forms[0].id)
  }

  @Test
  fun `Only forms belonging to user returned from database`() {
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM_2")

    // Verify the database is set up correctly
    val allForms = repo.findAll()
    Assertions.assertThat(allForms).hasSize(3)

    val orderForms = webTestClient.get()
      .uri("/api/ListForms")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(OrderForm::class.java)
      .hasSize(2)
  }

  @Test
  fun `Should return order if owned by the user`() {
    val order = createOrder()

    val orderForms = webTestClient.get()
      .uri("/api/GetForm?id=${order.id}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)
      .isEqualTo(order)
  }

  @Test
  fun `Should return not found if order does not exist`() {
    val orderForms = webTestClient.get()
      .uri("/api/GetForm?id=${UUID.randomUUID()}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should return not found if order belongs to another user`() {
    val order = createOrder("AUTH_ADM")

    val orderForms = webTestClient.get()
      .uri("/api/GetForm?id=${order.id}")
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }
}
