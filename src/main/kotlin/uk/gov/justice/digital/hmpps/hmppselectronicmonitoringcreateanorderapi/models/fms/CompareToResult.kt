package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.OrderChangeDetail

open class CompareToResult<T : OrderChangeDetail> {
  private val _messages = mutableListOf<String>()
  val messages: List<String> get() = _messages

  private val orderVariationTypes = mutableListOf<VariationType>()
  val orderVariationType: VariationType
    get() {
      return orderVariationTypes.minByOrNull { it.priority } ?: VariationType.OTHER
    }

  fun addChange(change: T) {
    _messages += change.message
    orderVariationTypes += change.orderVariationType
  }

  fun addOrderVariationType(variationType: VariationType) {
    orderVariationTypes += variationType
  }

  fun addMessage(message: String) {
    _messages += message
  }
}
