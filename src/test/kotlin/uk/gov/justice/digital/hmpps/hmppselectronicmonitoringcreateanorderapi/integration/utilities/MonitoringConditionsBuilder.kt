package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.*
import java.time.ZonedDateTime
import java.util.UUID

class MonitoringConditionsBuilder(var versionId: UUID) {
  var orderType: OrderType = OrderType.COMMUNITY
  var orderTypeDescription: OrderTypeDescription = OrderTypeDescription.DAPOL
  var startDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  var endDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)
  var curfew: Boolean = false
  var trail: Boolean = false
  var exclusionZone: Boolean = false
  var alcohol: Boolean = false
  var caseId: String = "d8ea62e61bb8d610a10c20e0b24bcb85"
  var conditionType: MonitoringConditionType = MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER
  var sentenceType: SentenceType = SentenceType.LIFE_SENTENCE
  var issp: YesNoUnknown = YesNoUnknown.YES

  fun build(): MonitoringConditions {
    return MonitoringConditions(
      versionId = versionId,
      orderType = orderType,
      orderTypeDescription = orderTypeDescription,
      startDate = startDate,
      endDate = endDate,
      curfew = curfew,
      trail = trail,
      exclusionZone = exclusionZone,
      alcohol = alcohol,
      caseId = caseId,
      conditionType = conditionType,
      sentenceType = sentenceType,
      issp = issp,
    )
  }
}