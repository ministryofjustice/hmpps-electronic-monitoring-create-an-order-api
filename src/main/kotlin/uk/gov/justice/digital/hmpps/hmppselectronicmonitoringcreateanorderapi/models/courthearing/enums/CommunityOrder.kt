package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.courthearing.enums

enum class CommunityOrder(val id: String) {
  // Community order England / Wales(COEW)
  COEW("418b3aa7-65ab-4a4a-bab9-2f96b698118c"),

  // Youth Rehabilitation Order England and Wales (YROEW)
  YROEW("73a4f6a2-b768-45de-beb7-3f4d2f933e11"),

  // Youth rehabilitation order with fostering England / Wales (YROFEW)
  YROFEW("ae8c21a9-cf2a-487b-8fae-58d50c7104f0"),

  // Youth rehabilitation order with intensive supervision and surveillance England / Wales (YROISS)
  YROISS("0b5ce679-b262-436d-8f94-aa78de85022a"),

  // Suspended sentence order - detention in a young offender institution (SUSPSD)
  SUSPSD("5679e5b7-0ca8-4d2a-ba80-7a50025fb589"),

  // Suspended sentence order - imprisonment (SUSPS)
  SUSPS("8b1cff00-a456-40da-9ce4-f11c20959084"),

  // Supervision default order(SDO)
  SDO("fd391847-f640-402e-a958-f33a014e6684"),

  // Suspended sentence order varied- imprisonment
  SUSPV("280a5606-c70a-432f-bdfe-a9dfd49f18c5"),

  // Youth rehabilitation order varied
  YROV("5cceca91-e6f2-471b-8919-8dfd3ad3c746"),

  // Suspended sentence order varited - detention
  SUSPVD("726c4beb-9ead-4b6b-bed9-b761b5b43553"),

  // DAPO
  DAPO("db5e6d5a-337f-4a68-b060-35a644c913ef"),

  // DAPO variation
  DAPOV("90f87813-e738-4c21-b122-5ebe93bb575f"),
  ;

  companion object {
    fun contains(id: String?) = entries.any { it.id == id }
  }
}
