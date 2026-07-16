package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Tuple
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultAddressDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultDeviceWearerDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultMonitoringConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.UUID

class OrderSearchRepositoryImpl(@PersistenceContext private val entityManager: EntityManager) : OrderSearchRepository {

  override fun searchOrders(criteria: OrderSearchCriteria): List<OrderSearchResultDto> {
    val cb = entityManager.criteriaBuilder
    val query = cb.createTupleQuery()

    val version = query.from(OrderVersion::class.java)
    val deviceWearer: Join<OrderVersion, DeviceWearer> = version.join("deviceWearer", JoinType.LEFT)
    val monitoringConditions: Join<OrderVersion, MonitoringConditions> =
      version.join("monitoringConditions", JoinType.LEFT)
    val primaryAddress: Join<OrderVersion, Address> = version.join("addresses", JoinType.LEFT)
    primaryAddress.on(cb.equal(primaryAddress.get<AddressType>("addressType"), AddressType.PRIMARY))

    query.multiselect(
      version.get<UUID>("orderId").alias("orderId"),
      version.get<OrderStatus>("status").alias("status"),
      version.get<RequestType>("type").alias("type"),
      version.get<OffsetDateTime>("fmsResultDate").alias("fmsResultDate"),
      deviceWearer.get<String>("firstName").alias("firstName"),
      deviceWearer.get<String>("lastName").alias("lastName"),
      deviceWearer.get<ZonedDateTime>("dateOfBirth").alias("dateOfBirth"),
      deviceWearer.get<Boolean>("adultAtTimeOfInstallation").alias("adultAtTimeOfInstallation"),
      deviceWearer.get<String>("nomisId").alias("nomisId"),
      deviceWearer.get<String>("pncId").alias("pncId"),
      deviceWearer.get<String>("deliusId").alias("deliusId"),
      deviceWearer.get<String>("prisonNumber").alias("prisonNumber"),
      deviceWearer.get<String>("homeOfficeReferenceNumber").alias("homeOfficeReferenceNumber"),
      deviceWearer.get<String>("complianceAndEnforcementPersonReference")
        .alias("complianceAndEnforcementPersonReference"),
      deviceWearer.get<String>("courtCaseReferenceNumber").alias("courtCaseReferenceNumber"),
      primaryAddress.get<String>("addressLine3").alias("addressLine3"),
      monitoringConditions.get<ZonedDateTime>("startDate").alias("startDate"),
      monitoringConditions.get<ZonedDateTime>("endDate").alias("endDate"),
    )

    query.where(*buildPredicates(criteria, cb, query, version, deviceWearer).toTypedArray())

    return entityManager.createQuery(query).resultList.map { it.toDto() }
  }

  private fun buildPredicates(
    criteria: OrderSearchCriteria,
    cb: CriteriaBuilder,
    query: jakarta.persistence.criteria.CriteriaQuery<Tuple>,
    version: Root<OrderVersion>,
    deviceWearer: Join<OrderVersion, DeviceWearer>,
  ): List<Predicate> {
    val predicates = mutableListOf<Predicate>()

    val latestVersion = query.subquery(Int::class.java)
    val subqueryRoot = latestVersion.from(OrderVersion::class.java)
    latestVersion.select(cb.max(subqueryRoot.get("versionId")))
    latestVersion.where(cb.equal(subqueryRoot.get<UUID>("orderId"), version.get<UUID>("orderId")))
    predicates.add(cb.equal(version.get<Int>("versionId"), latestVersion))

    predicates.add(
      cb.or(
        cb.equal(version.get<OrderStatus>("status"), OrderStatus.SUBMITTED),
        cb.equal(version.get<OrderStatus>("status"), OrderStatus.IN_PROGRESS),
      ),
    )

    val normalizedKeyword = criteria.searchTerm.trim().replace(Regex("\\s+"), " ").lowercase()
    if (normalizedKeyword.isNotEmpty()) {
      val searchTermPredicates = normalizedKeyword.split(" ").map { keywordPart ->
        isMatch(deviceWearer, cb, keywordPart)
      }
      predicates.add(cb.and(*searchTermPredicates.toTypedArray()))
    }

    val filter = criteria.tagFilter
    if (filter.tagGroups.isNotEmpty()) {
      val groupPredicates = filter.tagGroups.map { group ->
        val groupRequirement = group.map { tag -> hasTag(cb, version, tag) }
        cb.and(*groupRequirement.toTypedArray())
      }
      if (criteria.ownerCohort != null) {
        val ownerCohortQuery = cb.equal(
          cb.lower(version.get("ownerCohort")),
          criteria.ownerCohort.lowercase(),
        )
        predicates.add(cb.or(cb.or(*groupPredicates.toTypedArray()), ownerCohortQuery))
      } else {
        predicates.add(cb.or(*groupPredicates.toTypedArray()))
      }
    }

    if (filter.exclude.isNotEmpty()) {
      val excludeTagPredicates = filter.exclude.map { tag -> cb.not(hasTag(cb, version, tag)) }
      val notMatches = cb.and(*excludeTagPredicates.toTypedArray())
      predicates.add(cb.or(notMatches, cb.isNull(version.get<String>("tags"))))
    }

    return predicates
  }

  private fun isMatch(
    deviceWearer: Join<OrderVersion, DeviceWearer>,
    cb: CriteriaBuilder,
    keyword: String,
  ): Predicate {
    val searchableFields = listOf(
      DeviceWearer::firstName.name,
      DeviceWearer::lastName.name,
      DeviceWearer::nomisId.name,
      DeviceWearer::pncId.name,
      DeviceWearer::deliusId.name,
      DeviceWearer::prisonNumber.name,
      DeviceWearer::homeOfficeReferenceNumber.name,
      DeviceWearer::complianceAndEnforcementPersonReference.name,
      DeviceWearer::courtCaseReferenceNumber.name,
    )
    return cb.or(
      *searchableFields.map { field -> cb.like(cb.lower(deviceWearer.get(field)), keyword) }.toTypedArray(),
    )
  }

  private fun hasTag(cb: CriteriaBuilder, version: Root<OrderVersion>, tag: String): Predicate {
    val paddedTags = cb.concat(cb.concat(",", version.get("tags")), ",")
    return cb.like(cb.lower(paddedTags), "%,${tag.lowercase()},%")
  }

  private fun Tuple.toDto(): OrderSearchResultDto = OrderSearchResultDto(
    id = get("orderId", UUID::class.java),
    status = get("status", OrderStatus::class.java),
    type = get("type", RequestType::class.java),
    fmsResultDate = get("fmsResultDate", OffsetDateTime::class.java),
    deviceWearer = OrderSearchResultDeviceWearerDto(
      firstName = get("firstName", String::class.java),
      lastName = get("lastName", String::class.java),
      dateOfBirth = get("dateOfBirth", ZonedDateTime::class.java),
      adultAtTimeOfInstallation = get("adultAtTimeOfInstallation", Boolean::class.javaObjectType),
      nomisId = get("nomisId", String::class.java),
      pncId = get("pncId", String::class.java),
      deliusId = get("deliusId", String::class.java),
      prisonNumber = get("prisonNumber", String::class.java),
      homeOfficeReferenceNumber = get("homeOfficeReferenceNumber", String::class.java),
      complianceAndEnforcementPersonReference = get("complianceAndEnforcementPersonReference", String::class.java),
      courtCaseReferenceNumber = get("courtCaseReferenceNumber", String::class.java),
    ),
    addresses = get("addressLine3", String::class.java)
      ?.let { listOf(OrderSearchResultAddressDto(addressType = AddressType.PRIMARY, addressLine3 = it)) }
      ?: emptyList(),
    monitoringConditions = OrderSearchResultMonitoringConditionsDto(
      startDate = get("startDate", ZonedDateTime::class.java),
      endDate = get("endDate", ZonedDateTime::class.java),
    ),
  )
}
