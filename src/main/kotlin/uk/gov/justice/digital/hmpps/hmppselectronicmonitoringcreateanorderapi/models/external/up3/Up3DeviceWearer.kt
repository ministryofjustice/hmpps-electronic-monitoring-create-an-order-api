package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Up3DeviceWearer(
  var title: String,
  var firstName: String,
  var lastName: String,
  var alias: String,
  var dateOfBirth: String,
  var adultChild: String,
  var interpreterRequired: String,
  var language: String,
  var genderIdentity: String,
  var sex: String,
  var homeOfficeCaseReferenceNumber: String,
  var prisonNumber: String,
  var deliusId: String,
  var nomisId: String,
  var pncId: String,
  var cepr: String,
  var noFixedAddress: String,
  var address1: String,
  var address2: String,
  var address3: String,
  var address4: String,
  var addressPostCode: String,
  var secondaryAddress1: String,
  var secondaryAddress2: String,
  var secondaryAddress3: String,
  var secondaryAddress4: String,
  var secondaryAddressPostCode: String,
  var tertiaryAddress1: String,
  var tertiaryAddress2: String,
  var tertiaryAddress3: String,
  var tertiaryAddress4: String,
  var tertiaryAddressPostCode: String,
  var mappa: String,
  var mappaCaseType: String,
  var mappaCategory: String,
  var riskDetails: String,
  var riskCategories: List<Up3RiskCategory>,
  var parent: String,
  var guardian: String,
  var parentPhoneNumber: String,
  var disabilities: List<Up3Disability>,
) {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val londonTimeZone: ZoneId = ZoneId.of("Europe/London")
    private val log = LoggerFactory.getLogger(Up3DeviceWearer::class.java)
  }

  fun toDeviceWearer(versionId: UUID): DeviceWearer {
    val parsedDob = LocalDate.parse(dateOfBirth, dateFormatter).atStartOfDay(londonTimeZone)

    return DeviceWearer(
      versionId = versionId,
      firstName = firstName,
      lastName = lastName,
      alias = alias,
      dateOfBirth = parsedDob,
      adultAtTimeOfInstallation = adultChild == "adult",
      interpreterRequired = interpreterRequired == "true",
      language = language,
      gender = genderIdentity,
      sex = sex,
      pncId = pncId,
      nomisId = nomisId,
      deliusId = deliusId,
      prisonNumber = prisonNumber,
      homeOfficeReferenceNumber = homeOfficeCaseReferenceNumber,
      complianceAndEnforcementPersonReference = cepr,
      disabilities = disabilityList(),
    )
  }

  fun toAddress(versionId: UUID): Address? {
    if (noFixedAddress == "true") return null

    return buildAddress(
      versionId,
      address1,
      address2,
      address3,
      address4,
      addressPostCode,
      AddressType.PRIMARY,
    )
  }

  fun toSecondaryAddress(versionId: UUID): Address? {
    if (secondaryAddress1.isBlank()) return null

    return buildAddress(
      versionId = versionId,
      secondaryAddress1,
      secondaryAddress2,
      secondaryAddress3,
      secondaryAddress4,
      secondaryAddressPostCode,
      AddressType.SECONDARY,
    )
  }

  fun toTertiaryAddress(versionId: UUID): Address? {
    if (tertiaryAddress1.isBlank()) return null

    return buildAddress(
      versionId = versionId,
      tertiaryAddress1,
      tertiaryAddress2,
      tertiaryAddress3,
      tertiaryAddress4,
      tertiaryAddressPostCode,
      AddressType.TERTIARY,
    )
  }

  fun toMappa(versionId: UUID): Mappa {
    val level = MappaLevel.from(mappa)
    if (mappa.isNotBlank() && level == null) {
      log.error("Unmatched MAPPA level value: {}", mappa)
    }

    val categoryValue = mappaCategory.ifBlank { mappaCaseType }
    val category = MappaCategory.from(categoryValue)
    if (categoryValue.isNotBlank() && category == null) {
      log.error("Unmatched MAPPA category value: {}", categoryValue)
    }

    return Mappa(
      versionId = versionId,
      level = level,
      category = category,
      isMappa = if (level != null || category != null) YesNoUnknown.YES else null,
    )
  }

  fun toInstallationAndRisk(versionId: UUID): InstallationAndRisk {
    val matchedCategories = riskCategories.mapNotNull { riskCategory ->
      val match = RiskCategory.from(riskCategory.category)
      if (match == null) {
        log.error("Unmatched risk category value: {}", riskCategory.category)
      }
      match?.name
    }

    return InstallationAndRisk(
      versionId = versionId,
      riskDetails = riskDetails,
      riskCategory = matchedCategories.takeIf { it.isNotEmpty() }?.toTypedArray(),
    )
  }

  fun toResponsibleAdult(versionId: UUID): ResponsibleAdult? {
    val fullName: String
    val relationship: String
    when {
      parent.isNotBlank() -> {
        fullName = parent
        relationship = "Parent"
      }

      guardian.isNotBlank() -> {
        fullName = guardian
        relationship = "Guardian"
      }

      else -> return null
    }

    return ResponsibleAdult(
      versionId = versionId,
      fullName = fullName,
      relationship = relationship,
      contactNumber = parentPhoneNumber,
    )
  }

  private fun disabilityList(): String? {
    val matchedDisabilities = disabilities.mapNotNull { up3Disability ->
      val match = Disability.from(up3Disability.disability)
      if (match == null) {
        log.error("Unmatched disability value: {}", up3Disability.disability)
      }
      match?.name
    }

    return matchedDisabilities.takeIf { it.isNotEmpty() }?.joinToString(",")
  }

  private fun buildAddress(
    versionId: UUID,
    line1: String,
    line2: String,
    line3: String,
    line4: String,
    postcode: String,
    type: AddressType,
  ) = Address(
    versionId = versionId,
    addressLine1 = line1,
    addressLine2 = line2,
    addressLine3 = line3,
    addressLine4 = line4,
    postcode = postcode,
    addressType = type,
  )
}
