package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.adapter.out.persistence

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType

@Component
class OrderMapper {
  fun mapToDomainEntity(order: OrderJpaEntity): Order = Order(
    id = order.id,
    versionId = order.versionId,
    mappa = Mappa(order.id, order.versionId, order.mappa?.level, order.mappa?.category),
  )

  fun mapToJpaEntity(order: Order): OrderJpaEntity {
    // not perfect as mappa and other fields don't have their own models
    // at the domain level
    return OrderJpaEntity(
      id = order.id,
      versions = mutableListOf(
        OrderVersion(
          id = order.versionId,
          orderId = order.id,
          mappa = uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa(
            versionId = order.versionId,
            level = order.mappa?.level,
            category = order.mappa?.category,
          ),
          username = "blah",
          status = OrderStatus.IN_PROGRESS,
          dataDictionaryVersion = DataDictionaryVersion.DDV6,
          type = RequestType.REQUEST,
        ),
      ),
    )
  }
}
