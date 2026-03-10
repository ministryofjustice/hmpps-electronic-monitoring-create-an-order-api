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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus

class OrderSearchSpecification(private val criteria: OrderSearchCriteria) : Specification<Order> {
  private fun isLikeFirstName(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate = criteriaBuilder.like(
    criteriaBuilder.lower(
      deviceWearer.get(DeviceWearer::firstName.name),
    ),
    keyword,
  )

  private fun isLikeLastName(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate = criteriaBuilder.like(
    criteriaBuilder.lower(
      deviceWearer.get(DeviceWearer::lastName.name),
    ),
    keyword,
  )

  private fun isLikePIN(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate {
    val matchesNomisId = criteriaBuilder.like(
      criteriaBuilder.lower(deviceWearer.get(DeviceWearer::nomisId.name)),
      keyword,
    )
    val matchesPncId = criteriaBuilder.like(criteriaBuilder.lower(deviceWearer.get(DeviceWearer::pncId.name)), keyword)
    val matchesDeliusId =
      criteriaBuilder.like(criteriaBuilder.lower(deviceWearer.get(DeviceWearer::deliusId.name)), keyword)
    val matchesPrisonNumber =
      criteriaBuilder.like(criteriaBuilder.lower(deviceWearer.get(DeviceWearer::prisonNumber.name)), keyword)
    val matchesHomeOfficeRefNumber =
      criteriaBuilder.like(
        criteriaBuilder.lower(deviceWearer.get(DeviceWearer::homeOfficeReferenceNumber.name)),
        keyword,
      )
    val matchesComplianceAndEnforcementPersonRef = criteriaBuilder.like(
      criteriaBuilder.lower(deviceWearer.get(DeviceWearer::complianceAndEnforcementPersonReference.name)),
      keyword,
    )
    val matchesCourtCaseRefNumber = criteriaBuilder.like(
      criteriaBuilder.lower(deviceWearer.get(DeviceWearer::courtCaseReferenceNumber.name)),
      keyword,
    )

    return criteriaBuilder.or(
      matchesNomisId,
      matchesPncId,
      matchesDeliusId,
      matchesPrisonNumber,
      matchesHomeOfficeRefNumber,
      matchesComplianceAndEnforcementPersonRef,
      matchesCourtCaseRefNumber,
    )
  }

  private fun isMatch(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    criteriaBuilder: CriteriaBuilder,
    keyword: String,
  ): Predicate = criteriaBuilder.or(
    isLikeFirstName(deviceWearer, criteriaBuilder, keyword),
    isLikeLastName(deviceWearer, criteriaBuilder, keyword),
    isLikePIN(deviceWearer, criteriaBuilder, keyword),
  )

  private fun hasTag(criteriaBuilder: CriteriaBuilder, version: Join<Order, OrderVersion>, tag: String): Predicate {
    val paddedTags = criteriaBuilder.concat(
      criteriaBuilder.concat(",", version.get("tags")),
      ",",
    )
    return criteriaBuilder.like(criteriaBuilder.lower(paddedTags), "%,${tag.lowercase()},%")
  }

  override fun toPredicate(
    root: Root<Order>,
    @Nullable query: CriteriaQuery<*>?,
    criteriaBuilder: CriteriaBuilder,
  ): Predicate? {
    val version: Join<Order, OrderVersion> = root.join("versions")
    val deviceWearer: Join<OrderVersion, DeviceWearer> = version.join("deviceWearer")

    // Subquery to get the max version number
    val subquery = query?.subquery(Int::class.java)
    val subqueryRoot = subquery?.from(OrderVersion::class.java)
    subquery?.select(criteriaBuilder.max(subqueryRoot?.get("versionId")))
    subquery?.where(criteriaBuilder.equal(subqueryRoot?.get<Order>("order"), root))

    val predicates = mutableListOf(
      criteriaBuilder.equal(version.get<Int>("versionId"), subquery),
      criteriaBuilder.equal(version.get<String>("status"), OrderStatus.SUBMITTED),
    )

    val normalizedKeyword = this.criteria.searchTerm.trim().replace(Regex("\\s+"), " ").lowercase()
    if (normalizedKeyword.isNotEmpty()) {
      val searchTermPredicates =
        normalizedKeyword.split(" ")
          .map { keywordPart -> isMatch(deviceWearer, criteriaBuilder, keywordPart) }
      predicates.add(criteriaBuilder.and(*searchTermPredicates.toTypedArray()))
    }

    val filter = this.criteria.tagFilter
    // OR logic between groups of ANDs
    if (filter.tagGroups.isNotEmpty()) {
      val groupPredicates =
        filter.tagGroups.map { group ->
          val groupRequirement = group.map { tag -> hasTag(criteriaBuilder, version, tag) }
          criteriaBuilder.and(*groupRequirement.toTypedArray())
        }
      predicates.add(criteriaBuilder.or(*groupPredicates.toTypedArray()))
    }

    if (filter.noneOf.isNotEmpty()) {
      val excludeTagPredicates = filter.noneOf.map { tag -> criteriaBuilder.not(hasTag(criteriaBuilder, version, tag)) }
      val notMatches = criteriaBuilder.and(*excludeTagPredicates.toTypedArray())
      predicates.add(criteriaBuilder.or(notMatches, criteriaBuilder.isNull(version.get<String>("tags"))))
    }

    return criteriaBuilder.and(
      *predicates.toTypedArray(),
    )
  }
}
