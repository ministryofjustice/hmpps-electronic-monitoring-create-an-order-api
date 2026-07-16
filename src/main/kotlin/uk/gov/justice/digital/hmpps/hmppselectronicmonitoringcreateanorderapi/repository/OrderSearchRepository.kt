package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderSearchResultDto

interface OrderSearchRepository {
  fun searchOrders(criteria: OrderSearchCriteria): List<OrderSearchResultDto>
}
