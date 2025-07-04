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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria

class OrderListSpecification(private val criteria: OrderListCriteria) : Specification<Order> {
  private fun wildcard(str: String): String = "%$str%"

  private fun isOwnedByUser(
    version: Join<Order, OrderVersion>,
    criteriaBuilder: CriteriaBuilder,
    username: String,
  ): Predicate = criteriaBuilder.equal(
    version.get<String>(OrderVersion::username.name),
    username,
  )

  private fun isLikeFirstName(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate = criteriaBuilder.like(
    criteriaBuilder.lower(
      deviceWearer.get(DeviceWearer::firstName.name),
    ),
    wildcard(keyword).lowercase(),
  )

  private fun isLikeLastName(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate = criteriaBuilder.like(
    criteriaBuilder.lower(
      deviceWearer.get(DeviceWearer::lastName.name),
    ),
    wildcard(keyword).lowercase(),
  )

  private fun isLikeFullName(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate {
    var fullName = criteriaBuilder.concat(deviceWearer.get(DeviceWearer::firstName.name), " ")
    fullName = criteriaBuilder.concat(fullName, deviceWearer.get(DeviceWearer::lastName.name))

    val normalizedKeyword = keyword.trim().replace(Regex("\\s+"), " ").lowercase()
    return criteriaBuilder.like(
      criteriaBuilder.lower(fullName),
      wildcard(normalizedKeyword),
    )
  }

  override fun toPredicate(
    root: Root<Order>,
    @Nullable query: CriteriaQuery<*>?,
    criteriaBuilder: CriteriaBuilder,
  ): Predicate? {
    val predicates = mutableListOf<Predicate>()
    val version: Join<Order, OrderVersion> = root.join("versions")
    val deviceWearer: Join<OrderVersion, DeviceWearer> = version.join("deviceWearer")

    // Subquery to get the max version number
    val subquery = query?.subquery(Int::class.java)
    val subqueryRoot = subquery?.from(OrderVersion::class.java)
    subquery?.select(criteriaBuilder.max(subqueryRoot?.get<Int>("versionId")))
    subquery?.where(criteriaBuilder.equal(subqueryRoot?.get<Order>("order"), root))

    if (this.criteria.searchTerm.isNotEmpty()) {
      predicates.add(isLikeFirstName(deviceWearer, criteriaBuilder, this.criteria.searchTerm))
      predicates.add(isLikeLastName(deviceWearer, criteriaBuilder, this.criteria.searchTerm))
      predicates.add(isLikeFullName(deviceWearer, criteriaBuilder, this.criteria.searchTerm))
    }

    if (predicates.isNotEmpty()) {
      return criteriaBuilder.and(
        criteriaBuilder.equal(version.get<Int>("versionId"), subquery),
        isOwnedByUser(version, criteriaBuilder, this.criteria.username),
        criteriaBuilder.or(*predicates.toTypedArray()),
      )
    }

    return criteriaBuilder.and(
      criteriaBuilder.equal(version.get<Int>("versionId"), subquery),
      isOwnedByUser(version, criteriaBuilder, this.criteria.username),
    )
  }
}
