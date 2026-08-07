package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3

import com.fasterxml.jackson.annotation.JsonProperty
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
  @JsonProperty("first_name")
  var firstName: String,
  @JsonProperty("middle_name")
  var middleName: String,
  @JsonProperty("last_name")
  var lastName: String,
  var alias: String,
  @JsonProperty("date_of_birth")
  var dateOfBirth: String,
  @JsonProperty("adult_child")
  var adultChild: String,
  @JsonProperty("interpreter_required")
  var interpreterRequired: String,
  var language: String,
  @JsonProperty("gender_identity")
  var genderIdentity: String,
  var sex: String,
  @JsonProperty("home_office_case_reference_number")
  var homeOfficeCaseReferenceNumber: String,
  @JsonProperty("prison_number")
  var prisonNumber: String,
  @JsonProperty("delius_id")
  var deliusId: String,
  @JsonProperty("nomis_id")
  var nomisId: String,
  @JsonProperty("pnc_id")
  var pncId: String,
  var cepr: String,
  @JsonProperty("no_fixed_address")
  var noFixedAddress: String,
  @JsonProperty("address_1")
  var address1: String,
  @JsonProperty("address_2")
  var address2: String,
  @JsonProperty("address_3")
  var address3: String,
  @JsonProperty("address_4")
  var address4: String,
  @JsonProperty("address_post_code")
  var addressPostCode: String,
  @JsonProperty("secondary_address_1")
  var secondaryAddress1: String,
  @JsonProperty("secondary_address_2")
  var secondaryAddress2: String,
  @JsonProperty("secondary_address_3")
  var secondaryAddress3: String,
  @JsonProperty("secondary_address_4")
  var secondaryAddress4: String,
  @JsonProperty("secondary_address_post_code")
  var secondaryAddressPostCode: String,
  @JsonProperty("tertiary_address_1")
  var tertiaryAddress1: String,
  @JsonProperty("tertiary_address_2")
  var tertiaryAddress2: String,
  @JsonProperty("tertiary_address_3")
  var tertiaryAddress3: String,
  @JsonProperty("tertiary_address_4")
  var tertiaryAddress4: String,
  @JsonProperty("tertiary_address_post_code")
  var tertiaryAddressPostCode: String,
  var mappa: String,
  @JsonProperty("mappa_case_type")
  var mappaCaseType: String,
  @JsonProperty("mappa_category")
  var mappaCategory: String,
  @JsonProperty("risk_details")
  var riskDetails: String,
  @JsonProperty("risk_categories")
  var riskCategories: List<Up3RiskCategory>,
  var parent: String,
  var guardian: String,
  @JsonProperty("parent_phone_number")
  var parentPhoneNumber: String,
  @JsonProperty("disability")
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
      middleName = middleName,
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
