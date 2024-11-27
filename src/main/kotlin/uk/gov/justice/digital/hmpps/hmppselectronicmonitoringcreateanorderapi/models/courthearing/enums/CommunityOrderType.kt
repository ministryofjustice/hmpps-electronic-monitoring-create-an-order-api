package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums

enum class CommunityOrderType(val uuid: String) {
  ALCOHOL_ABSTAIN_MONITORING("d54c3093-6b9b-4b61-80cf-a0bf4ed5d2e8"),
  EXCLUSION_ZONE("091cd45b-4312-476e-a122-18cc02fd1699"),
  INCLUSION_ZONE("9b216a08-4df8-41c2-a947-66506cd1e1b5"),
  COMMUNITY_ORDER_CURFEW("06b4c31d-1b3d-4850-b64c-4cad870b3a25"),
  ;

  companion object {
    fun from(uuid: String?): CommunityOrderType? {
      return CommunityOrderType.entries.firstOrNull {
        it.uuid == uuid
      }
    }
  }
}

enum class BailOrderType(val uuid: String) {
  CURFEW("629f6897-a46f-492e-9691-5226ee7810b7"),

  EXCLUSION_NOT_ENTER_A_PLACE("c1d490ed-1754-43b8-a485-fdab1a25f8cb"),

  EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT("dfa19118-e944-43f4-93b2-2ed49df5553f"),

  INCLUSION_SPECIFIED_RADIUS("c9ae30f1-3c3b-4edf-a7d4-49bd027977c3"),
  ;

  companion object {
    fun from(uuid: String?): BailOrderType? {
      return BailOrderType.entries.firstOrNull {
        it.uuid == uuid
      }
    }
  }
}
