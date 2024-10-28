package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Hearing
import uk.gov.justice.digital.hmpps.courthearingeventreceiver.model.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order

@Service
class HearingEventHandler {

  companion object {
    fun isTaggableOffence(offence: Offence): Boolean {
      return offence.judicialResults.any {
          judicialResults ->
        // Community order England_Wales, Alcohol abstinence and monitoring
        judicialResults.judicialResultTypeId == "d54c3093-6b9b-4b61-80cf-a0bf4ed5d2e8"
      }
    }
  }

  fun getOrdersFromHearing(hearing: Hearing): List<Order> {
    val orders = mutableListOf<Order>()
    val defendantOffences = hearing.prosecutionCases
      .flatMap { it.defendants }
      .filter { defendant -> defendant.offences.any { isTaggableOffence(it) } }
      .groupBy { it.id }
      .mapValues { (_, defendants) ->
        defendants.flatMap { it.offences }.filter { isTaggableOffence(it) }.toMutableList()
      }.toMutableMap()

    return orders
  }
}
