package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import org.springframework.lang.Nullable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus

class OrderListSpecification(private val criteria: OrderListCriteria) : Specification<Order> {
  private fun isOwnedByUser(
    version: Join<Order, OrderVersion>,
    criteriaBuilder: CriteriaBuilder,
    username: String,
  ): Predicate = criteriaBuilder.equal(
    version.get<String>(OrderVersion::username.name),
    username,
  )

  private fun isValidOrderStatus(version: Join<Order, OrderVersion>, criteriaBuilder: CriteriaBuilder): Predicate {
    val inProgress = criteriaBuilder.equal(version.get<String>(OrderVersion::status.name), OrderStatus.IN_PROGRESS)
    val failed = criteriaBuilder.equal(version.get<String>(OrderVersion::status.name), OrderStatus.ERROR)
    return criteriaBuilder.or(inProgress, failed)
  }

  override fun toPredicate(
    root: Root<Order>,
    @Nullable query: CriteriaQuery<*>?,
    criteriaBuilder: CriteriaBuilder,
  ): Predicate? {
    val version: Join<Order, OrderVersion> = root.join("versions")

    // Subquery to get the max version number
    val subquery = query?.subquery(Int::class.java)
    val subqueryRoot = subquery?.from(OrderVersion::class.java)
    subquery?.select(criteriaBuilder.max(subqueryRoot?.get<Int>("versionId")))
    subquery?.where(criteriaBuilder.equal(subqueryRoot?.get<Order>("order"), root))

    return criteriaBuilder.and(
      criteriaBuilder.equal(version.get<Int>("versionId"), subquery),
      isOwnedByUser(version, criteriaBuilder, this.criteria.username),
      isValidOrderStatus(version, criteriaBuilder),
    )
  }
}
