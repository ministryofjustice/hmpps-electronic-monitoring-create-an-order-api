package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class DataDictionaryVersion {
  DDV4,
  DDV5,
  DDV6,
  ;

  companion object {

    val versionList = listOf(
      DDV4,
      DDV5,
      DDV6,
    )
    fun isVersionSameOrAbove(orderVersion: DataDictionaryVersion, expectedVersion: DataDictionaryVersion): Boolean {
      val expectedVersionIndex = versionList.indexOf(expectedVersion)
      val orderVersionIndex = versionList.indexOf(orderVersion)
      return orderVersionIndex >= expectedVersionIndex
    }
  }
}
