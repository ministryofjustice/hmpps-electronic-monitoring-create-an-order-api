package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import org.springframework.lang.Nullable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria

class OrderSpecification(private val criteria: OrderSearchCriteria) : Specification<Order> {
  private fun wildcard(str: String): String {
    return "%$str%"
  }

  private fun isOwnedByUser(root: Root<Order>, criteriaBuilder: CriteriaBuilder, username: String): Predicate {
    return criteriaBuilder.equal(root.get<String>(Order::username.name), username)
  }

  private fun isLikeFirstName(
    deviceWearer: Join<Order, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate {
    return criteriaBuilder.like(
      criteriaBuilder.lower(
        deviceWearer.get(DeviceWearer::firstName.name),
      ),
      wildcard(keyword).lowercase(),
    )
  }

  private fun isLikeLastName(
    deviceWearer: Join<Order, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate {
    return criteriaBuilder.like(
      criteriaBuilder.lower(
        deviceWearer.get(DeviceWearer::lastName.name),
      ),
      wildcard(keyword).lowercase(),
    )
  }

  override fun toPredicate(
    root: Root<Order>,
    @Nullable query: CriteriaQuery<*>?,
    criteriaBuilder: CriteriaBuilder,
  ): Predicate? {
    val predicates = mutableListOf<Predicate>()
    val deviceWearer: Join<Order, DeviceWearer> = root.join("deviceWearer")

    if (this.criteria.searchTerm.isNotEmpty()) {
      predicates.add(isLikeFirstName(deviceWearer, criteriaBuilder, this.criteria.searchTerm))
      predicates.add(isLikeLastName(deviceWearer, criteriaBuilder, this.criteria.searchTerm))
    }

    if (predicates.isNotEmpty()) {
      return criteriaBuilder.and(
        isOwnedByUser(root, criteriaBuilder, this.criteria.username),
        criteriaBuilder.or(*predicates.toTypedArray()),
      )
    }

    return criteriaBuilder.and(
      isOwnedByUser(root, criteriaBuilder, this.criteria.username),
    )
  }
}
