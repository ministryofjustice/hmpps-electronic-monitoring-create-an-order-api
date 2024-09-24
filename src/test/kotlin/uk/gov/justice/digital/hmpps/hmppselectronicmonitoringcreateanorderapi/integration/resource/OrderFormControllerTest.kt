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
      .uri("/api/CreateForm?title=mockTitle")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    val forms = repo.findAll()
    Assertions.assertThat(forms).hasSize(1)
    Assertions.assertThat(forms[0]).isEqualTo(result.returnResult().responseBody)
    Assertions.assertThat(forms[0].title).isEqualTo("mockTitle")
    Assertions.assertThat(forms[0].username).isEqualTo("AUTH_ADM")
    Assertions.assertThat(forms[0].status).isEqualTo(FormStatus.IN_PROGRESS)
    Assertions.assertThat(forms[0].id).isNotNull()
    Assertions.assertThat(UUID.fromString(forms[0].id.toString())).isEqualTo(forms[0].id)
  }

  @Test
  fun `Only forms belonging to user returned from database`() {
    val result1 = webTestClient.get()
      .uri("/api/CreateForm?title=mockTitle")
      .headers(setAuthorisation("User1"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    val result2 = webTestClient.get()
      .uri("/api/CreateForm?title=mockTitle")
      .headers(setAuthorisation("User1"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    val result3 = webTestClient.get()
      .uri("/api/CreateForm?title=mockTitle")
      .headers(setAuthorisation("User2"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    // Verify the database is set up correctly
    val allForms = repo.findAll()
    Assertions.assertThat(allForms).hasSize(3)

    val orderForms = webTestClient.get()
      .uri("/api/ListForms")
      .headers(setAuthorisation("User1"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(OrderForm::class.java)
      .hasSize(2)
  }
}
