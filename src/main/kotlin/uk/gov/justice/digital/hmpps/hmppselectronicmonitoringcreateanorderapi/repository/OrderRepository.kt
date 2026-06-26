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
  JpaRepository<Order, UUID> {

  @Query(
    """
    SELECT ov.id, ov.status, ov.type, dw AS deviceWearer, ip AS interestedParties 
    FROM OrderVersion ov 
    LEFT JOIN ov.deviceWearer dw 
    LEFT JOIN ov.interestedParties ip
    WHERE ov.versionId = (SELECT MAX(ov2.versionId) FROM OrderVersion ov2 WHERE ov2.orderId = ov.orderId)
    AND ov.username = :username
    AND ov.status IN ('IN_PROGRESS', 'ERROR')
    """,
  )
  fun findOrderInformation(@Param("username") username: String): List<OrderVersionListInformation>
}
