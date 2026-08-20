package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.util.UUID

data class Address(
  val noFixedAbode: Boolean?,
  val postcode: String?,
  val status: CodeDescription?,
  val buildingNumber: String?,
  val buildingName: String?,
  val subBuildingName: String?,
  val postTown: String?,
  val county: String?,
  val thoroughfareName: String?,
  val contacts: List<Contact>,
) {
  fun isPrimaryAddress(): Boolean = this.status?.code == "M"

  fun isSecondaryAddress(): Boolean = this.status?.code == "S"

  fun toCemoAddress(versionId: UUID): Address = Address(
    versionId = versionId,
    addressLine1 = addressLineOne(),
    addressLine2 = "",
    addressLine3 = postTown?.toTitleCase() ?: "",
    addressLine4 = county?.toTitleCase() ?: "",
    postcode = postcode ?: "",
    addressType = addressType(),
  )

  private fun addressType(): AddressType = if (isPrimaryAddress()) AddressType.PRIMARY else AddressType.SECONDARY

  private fun addressLineOne(): String {
    val buildingId = buildingNumber.takeIf { !it.isNullOrEmpty() } ?: buildingName.takeIf { !it.isNullOrEmpty() } ?: ""

    if (thoroughfareName.isNullOrEmpty()) {
      return buildingId
    }

    return "$buildingId $thoroughfareName".toTitleCase()
  }

  private fun String.toTitleCase(): String = lowercase().split(" ").joinToString(" ") {
    it.replaceFirstChar { char -> char.uppercaseChar() }
  }
}
