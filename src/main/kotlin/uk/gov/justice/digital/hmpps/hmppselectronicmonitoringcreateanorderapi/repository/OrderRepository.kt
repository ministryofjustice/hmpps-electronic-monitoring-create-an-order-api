package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.projections.OrderVersionListInformation
import java.util.*

@Repository
interface OrderRepository :
  PagingAndSortingRepository<Order, UUID>,
  JpaSpecificationExecutor<Order>,
  JpaRepository<Order, UUID>,
  OrderSearchRepository {

  @Query(
    """
    SELECT ov.orderId as id, ov.id AS versionId, ov.status AS status, ov.type AS type, dw.firstName AS firstName, dw.lastName AS lastName, ip.notifyingOrganisation AS notifyingOrganisation, ov.lastUpdatedBy AS lastUpdatedBy, ov.lastUpdatedDateTime AS lastUpdatedDateTime
    FROM OrderVersion ov 
    LEFT JOIN ov.deviceWearer dw 
    LEFT JOIN ov.interestedParties ip
    WHERE ov.versionId = (SELECT MAX(ov2.versionId) FROM OrderVersion ov2 WHERE ov2.orderId = ov.orderId)
    AND ov.username = :username
    AND ov.status = 'IN_PROGRESS'
    ORDER BY ov.lastUpdatedDateTime DESC 
    """,
  )
  fun findMyOrders(@Param("username") username: String): List<OrderVersionListInformation>

  @Query(
    """
    SELECT ov.orderId as id, ov.id AS versionId, ov.status AS status, ov.type AS type, dw.firstName AS firstName, dw.lastName AS lastName, ip.notifyingOrganisation AS notifyingOrganisation, ov.lastUpdatedBy AS lastUpdatedBy, ov.lastUpdatedDateTime AS lastUpdatedDateTime
    FROM OrderVersion ov 
    LEFT JOIN ov.deviceWearer dw 
    LEFT JOIN ov.interestedParties ip
    WHERE ov.versionId = (SELECT MAX(ov2.versionId) FROM OrderVersion ov2 WHERE ov2.orderId = ov.orderId)
    AND ov.username = :username
    AND ov.status = 'ERROR'
    ORDER BY ov.lastUpdatedDateTime DESC 
    """,
  )
  fun findFailedOrders(@Param("username") username: String): List<OrderVersionListInformation>

  @Query(
    """
    SELECT ov.orderId as id, ov.id AS versionId, ov.status AS status, ov.type AS type, dw.firstName AS firstName, dw.lastName AS lastName, ip.notifyingOrganisation AS notifyingOrganisation, ov.lastUpdatedBy AS lastUpdatedBy, ov.lastUpdatedDateTime AS lastUpdatedDateTime
    FROM OrderVersion ov 
    LEFT JOIN ov.deviceWearer dw 
    LEFT JOIN ov.interestedParties ip
    WHERE ov.versionId = (SELECT MAX(ov2.versionId) FROM OrderVersion ov2 WHERE ov2.orderId = ov.orderId)
    AND ov.ownerCohort IN :prisonNames
    AND ov.status = 'IN_PROGRESS'
    ORDER BY ov.lastUpdatedDateTime DESC 
    """,
  )
  fun findPrisonOrders(@Param("prisonNames") prisonNames: List<String>): List<OrderVersionListInformation>
}
