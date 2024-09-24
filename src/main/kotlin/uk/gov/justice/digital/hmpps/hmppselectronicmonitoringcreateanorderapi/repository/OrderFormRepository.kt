package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import java.util.UUID

@Repository
interface OrderFormRepository : JpaRepository<OrderForm, UUID> {
  fun findByUsername(username: String): List<OrderForm>

  fun findByUsernameAndId(username: String, id: UUID): OrderForm
}
