package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType

enum class CommunityOrderType(val uuid: String) {
  ALCOHOL_ABSTAIN_MONITORING("d54c3093-6b9b-4b61-80cf-a0bf4ed5d2e8"),
  EXCLUSION_ZONE("091cd45b-4312-476e-a122-18cc02fd1699"),
  INCLUSION_ZONE("9b216a08-4df8-41c2-a947-66506cd1e1b5"),
  COMMUNITY_ORDER_CURFEW("06b4c31d-1b3d-4850-b64c-4cad870b3a25"),
  TRAIL_MONITORING("45ea2d9f-ddd8-4c39-9585-780746ee6a8d"),
  YOUTH_CURFEW("d79c92bb-c301-491a-b4d2-cb86e473b5af"),
  YOUTH_TRAIL("c7cdb5ba-e5a9-4145-a8b3-ecf5f28664a6"),
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
  CURFEW("fa49f99a-a2f1-4d4a-bd14-d18ab1c9eca5"),

  EXCLUSION_NOT_ENTER_A_PLACE("c1d490ed-1754-43b8-a485-fdab1a25f8cb"),

  EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT("dfa19118-e944-43f4-93b2-2ed49df5553f"),

  EXCLUSION_EXCEPT_ACCOMPANIED_BY_OFFICER("d709a455-02cc-40d9-b3d2-e4194dc46792"),

  INCLUSION_SPECIFIED_RADIUS("c9ae30f1-3c3b-4edf-a7d4-49bd027977c3"),

  INCLUSION_NOT_TO_LEAVE("ac44c4ed-c77c-4552-aed7-b4f05f1dc9db"),
  ;

  companion object {

    val ENFORCEMENT_ZONE_IDS = mapOf(
      EXCLUSION_NOT_ENTER_A_PLACE to EnforcementZoneType.EXCLUSION,
      EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT to EnforcementZoneType.EXCLUSION,
      EXCLUSION_EXCEPT_ACCOMPANIED_BY_OFFICER to EnforcementZoneType.EXCLUSION,
      INCLUSION_SPECIFIED_RADIUS to EnforcementZoneType.INCLUSION,
      INCLUSION_NOT_TO_LEAVE to EnforcementZoneType.INCLUSION,
    )
    fun from(uuid: String?): BailOrderType? {
      return BailOrderType.entries.firstOrNull {
        it.uuid == uuid
      }
    }
  }
}
