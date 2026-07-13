package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.time.OffsetDateTime
import java.util.*

@Service
abstract class OrderSectionServiceBase {
  @Autowired
  lateinit var orderRepo: OrderRepository

  internal fun findOrder(id: UUID, username: String): Order {
    val order = orderRepo.findById(id).orElseThrow {
      EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }

    if (order.username != username) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }
    return order
  }

  internal fun findEditableOrder(id: UUID, username: String): Order {
    val order = findOrder(id, username)
    if (order.status !== OrderStatus.IN_PROGRESS) {
      throw EntityNotFoundException(ValidationErrors.OrderSectionServiceBase.noEditableOrderExists(id))
    }

    return order
  }

  internal fun updateLastUpdatedByAndSaveOrder(order: Order, interestedParties: InterestedParties? = null): Order {
    val authentication = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    order.lastUpdatedBy = authentication.getUserFullName()
    order.lastUpdatedDateTime = OffsetDateTime.now()
    if (interestedParties != null) {
      order.ownerCohort = getOwnerCohort(interestedParties)
    }

    return orderRepo.save(order)
  }

  internal fun getOwnerCohort(interestedParties: InterestedParties): String? =
    when (interestedParties.notifyingOrganisation) {
      NotifyingOrganisationDDv5.PRISON.name -> interestedParties.notifyingOrganisationName
      else -> interestedParties.notifyingOrganisation
    }
}
