package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository :
  PagingAndSortingRepository<OrderJpaEntity, UUID>,
  JpaSpecificationExecutor<OrderJpaEntity>,
  JpaRepository<OrderJpaEntity, UUID>
