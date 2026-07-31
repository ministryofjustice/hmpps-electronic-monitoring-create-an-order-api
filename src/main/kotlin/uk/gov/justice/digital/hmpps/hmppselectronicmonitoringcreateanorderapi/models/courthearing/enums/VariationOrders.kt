package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums

class VariationOrders {

  companion object {

    val VARIATION_UUIDS = listOf(
      BailOrRemandToCareCondition.RCBV.id,
      CommunityOrder.SUSPV.id,
      CommunityOrder.YROV.id,
      CommunityOrder.SUSPVD.id
    )

    fun contains(id: String?) = VARIATION_UUIDS.contains(id)
  }
}
