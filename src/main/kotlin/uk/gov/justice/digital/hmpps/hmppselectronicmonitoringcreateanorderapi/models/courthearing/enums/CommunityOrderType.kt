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
  YOUTH_EXCLUSION("416684ab-755f-4958-ab83-2795a21d62a3"),
  SUPERVISION_CURFEW("4b72c78e-7571-4dd1-b719-5186e3a2cf80"),
  ;

  companion object {
    fun from(uuid: String?): CommunityOrderType? = CommunityOrderType.entries.firstOrNull {
      it.uuid == uuid
    }
  }
}

enum class BailOrderType(val uuid: String) {
  CURFEW("fa49f99a-a2f1-4d4a-bd14-d18ab1c9eca5"),

  MUST_STAY_INDOORS_AT_HOME_ADDRESS("f2cab905-935b-49e5-9f82-badcfb6c6c60"),

  REMAND_TO_CARE_MUST("671c65dc-8406-4923-bff2-e193a7fbd489"),

  EXCLUSION_NOT_ENTER_A_PLACE("c1d490ed-1754-43b8-a485-fdab1a25f8cb"),

  EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT("dfa19118-e944-43f4-93b2-2ed49df5553f"),

  EXCLUSION_EXCEPT_ACCOMPANIED_BY_OFFICER("d709a455-02cc-40d9-b3d2-e4194dc46792"),

  EXCLUSION_NOT_GO_WITHIN_A_RADIUS("9da5909e-d715-4f79-a1b8-34780c57acf1"),

  EXCLUSION_MUST_NOT_ENTER("2751bd8f-897a-4aa0-b26d-b7bf7b18acff"),

  EXCLUSION_NOT_ENTER_OTHER_THAN_ATTEND_COURT_OR_APPOINTMENT("f68c277a-4f7d-4da5-bd8a-396e6671e05c"),

  EXCLUSION_WITH_GPS_TAG("4c79ea50-6e14-4c47-8836-c0d0f647e45a"),

  EXCLUSION_MUST_NOT_GO_WITHIN("6f2e2861-0ad4-4721-b0d2-65b1d5f16895"),

  INCLUSION_SPECIFIED_RADIUS("c9ae30f1-3c3b-4edf-a7d4-49bd027977c3"),

  INCLUSION_NOT_TO_LEAVE("ac44c4ed-c77c-4552-aed7-b4f05f1dc9db"),

  ;

  companion object {

    val REMAND_TO_CARE_CURFEWS = listOf(
      MUST_STAY_INDOORS_AT_HOME_ADDRESS.uuid,
      REMAND_TO_CARE_MUST.uuid,
    )

    val ENFORCEMENT_ZONE_IDS = mapOf(
      EXCLUSION_NOT_ENTER_A_PLACE to EnforcementZoneType.EXCLUSION,
      EXCLUSION_EXCEPT_COURT_OR_APPOINTMENT to EnforcementZoneType.EXCLUSION,
      EXCLUSION_EXCEPT_ACCOMPANIED_BY_OFFICER to EnforcementZoneType.EXCLUSION,
      EXCLUSION_NOT_GO_WITHIN_A_RADIUS to EnforcementZoneType.EXCLUSION,
      EXCLUSION_WITH_GPS_TAG to EnforcementZoneType.EXCLUSION,
      EXCLUSION_MUST_NOT_ENTER to EnforcementZoneType.EXCLUSION,
      EXCLUSION_NOT_ENTER_OTHER_THAN_ATTEND_COURT_OR_APPOINTMENT to EnforcementZoneType.EXCLUSION,
      EXCLUSION_MUST_NOT_GO_WITHIN to EnforcementZoneType.EXCLUSION,
      INCLUSION_SPECIFIED_RADIUS to EnforcementZoneType.INCLUSION,
      INCLUSION_NOT_TO_LEAVE to EnforcementZoneType.INCLUSION,
    )
    fun from(uuid: String?): BailOrderType? = BailOrderType.entries.firstOrNull {
      it.uuid == uuid
    }
  }
}
