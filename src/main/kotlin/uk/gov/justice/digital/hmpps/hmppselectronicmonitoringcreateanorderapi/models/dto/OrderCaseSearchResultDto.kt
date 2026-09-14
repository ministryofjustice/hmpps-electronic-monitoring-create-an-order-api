package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import java.util.UUID

data class OrderCaseSearchResultDto(val id: UUID, val versions: List<OrderVersion>)
