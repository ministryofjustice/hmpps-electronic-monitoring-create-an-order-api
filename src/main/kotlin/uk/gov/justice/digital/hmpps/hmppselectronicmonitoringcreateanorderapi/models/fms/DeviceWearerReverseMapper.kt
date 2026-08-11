package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer as CemoDeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability as DisabilityEnum

private val log = LoggerFactory.getLogger(DeviceWearer::class.java)

fun DeviceWearer.toDeviceWearer(versionId: UUID): CemoDeviceWearer {
  val parsedDob = LocalDate.parse(dateOfBirth ?: "", FmsDates.dateFormatter).atStartOfDay(FmsDates.londonTimeZone)

  return CemoDeviceWearer(
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
    homeOfficeReferenceNumber = homeOfficeReferenceNumber,
    complianceAndEnforcementPersonReference = complianceAndEnforcementPersonReference,
    disabilities = disabilityList(),
  )
}

fun DeviceWearer.toAddress(versionId: UUID): Address? {
  if (noFixedAddress == "true") return null

  return buildAddress(
    versionId,
    address1 ?: "",
    address2 ?: "",
    address3 ?: "",
    address4 ?: "",
    addressPostCode ?: "",
    AddressType.PRIMARY,
  )
}

fun DeviceWearer.toSecondaryAddress(versionId: UUID): Address? {
  if (secondaryAddress1.isNullOrBlank()) return null

  return buildAddress(
    versionId = versionId,
    secondaryAddress1 ?: "",
    secondaryAddress2 ?: "",
    secondaryAddress3 ?: "",
    secondaryAddress4 ?: "",
    secondaryAddressPostCode ?: "",
    AddressType.SECONDARY,
  )
}

fun DeviceWearer.toTertiaryAddress(versionId: UUID): Address? {
  if (tertiaryAddress1.isNullOrBlank()) return null

  return buildAddress(
    versionId = versionId,
    tertiaryAddress1 ?: "",
    tertiaryAddress2 ?: "",
    tertiaryAddress3 ?: "",
    tertiaryAddress4 ?: "",
    tertiaryAddressPostCode ?: "",
    AddressType.TERTIARY,
  )
}

fun DeviceWearer.toMappa(versionId: UUID): Mappa {
  val level = MappaLevel.from(mappa)
  if (!mappa.isNullOrBlank() && level == null) {
    log.error("Unmatched MAPPA level value: {}", mappa)
  }

  val categoryValue = mappaCategory?.takeIf { it.isNotBlank() } ?: mappaCaseType
  val category = MappaCategory.from(categoryValue)
  if (!categoryValue.isNullOrBlank() && category == null) {
    log.error("Unmatched MAPPA category value: {}", categoryValue)
  }

  return Mappa(
    versionId = versionId,
    level = level,
    category = category,
    isMappa = if (level != null || category != null) YesNoUnknown.YES else null,
  )
}

fun DeviceWearer.toInstallationAndRisk(versionId: UUID): InstallationAndRisk {
  val matchedCategories = riskCategory.orEmpty().mapNotNull { entry ->
    val match = RiskCategory.from(entry.category)
    if (match == null) {
      log.error("Unmatched risk category value: {}", entry.category)
    }
    match?.name
  }

  return InstallationAndRisk(
    versionId = versionId,
    riskDetails = riskDetails,
    riskCategory = matchedCategories.takeIf { it.isNotEmpty() }?.toTypedArray(),
  )
}

fun DeviceWearer.toResponsibleAdult(versionId: UUID): ResponsibleAdult? {
  val fullName: String
  val relationship: String
  when {
    !parent.isNullOrBlank() -> {
      fullName = parent!!
      relationship = "Parent"
    }

    !guardian.isNullOrBlank() -> {
      fullName = guardian!!
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

private fun DeviceWearer.disabilityList(): String? {
  val matchedDisabilities = disability.orEmpty().mapNotNull { entry ->
    val match = DisabilityEnum.from(entry.disability)
    if (match == null) {
      log.error("Unmatched disability value: {}", entry.disability)
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
