package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Mappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Gender
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RiskCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Sex
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.formatters.PhoneNumberFormatter
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer as CemoDeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability as DisabilityEnum
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability as FmsDisability

data class DeviceWearer(
  var title: String? = "",

  @JsonProperty("first_name")
  var firstName: String? = "",

  @JsonProperty("middle_name")
  var middleName: String? = "",

  @JsonProperty("last_name")
  var lastName: String? = "",

  var alias: String? = "",

  @JsonProperty("date_of_birth")
  var dateOfBirth: String? = "",

  @JsonProperty("adult_child")
  var adultChild: String? = "",

  var sex: String? = "",

  @JsonProperty("gender_identity")
  var genderIdentity: String? = "",

  var disability: List<FmsDisability>? = emptyList(),

  @JsonProperty("address_1")
  var address1: String? = "",

  @JsonProperty("address_2")
  var address2: String? = "",

  @JsonProperty("address_3")
  var address3: String? = "",

  @JsonProperty("address_4")
  var address4: String? = "",

  @JsonProperty("address_post_code")
  var addressPostCode: String? = "",

  @JsonProperty("no_fixed_address")
  var noFixedAddress: String? = "false",

  @JsonProperty("secondary_address_1")
  var secondaryAddress1: String? = "",

  @JsonProperty("secondary_address_2")
  var secondaryAddress2: String? = "",

  @JsonProperty("secondary_address_3")
  var secondaryAddress3: String? = "",

  @JsonProperty("secondary_address_4")
  var secondaryAddress4: String? = "",

  @JsonProperty("secondary_address_post_code")
  var secondaryAddressPostCode: String? = "",

  @JsonProperty("tertiary_address_1")
  var tertiaryAddress1: String? = "",

  @JsonProperty("tertiary_address_2")
  var tertiaryAddress2: String? = "",

  @JsonProperty("tertiary_address_3")
  var tertiaryAddress3: String? = "",

  @JsonProperty("tertiary_address_4")
  var tertiaryAddress4: String? = "",

  @JsonProperty("tertiary_address_post_code")
  var tertiaryAddressPostCode: String? = "",

  @JsonProperty("phone_number")
  var phoneNumber: String? = "",

  @JsonProperty("risk_serious_harm")
  var riskSeriousHarm: String? = "",

  @JsonProperty("risk_self_harm")
  var riskSelfHarm: String? = "",

  @JsonProperty("risk_details")
  var riskDetails: String? = "",

  var mappa: String? = "",

  @JsonProperty("mappa_case_type")
  var mappaCaseType: String? = "",

  @JsonProperty("mappa_category")
  var mappaCategory: String? = null,

  @JsonProperty("risk_categories")
  var riskCategory: List<FmsRiskCategory>? = emptyList(),

  @JsonProperty("responsible_adult_required")
  var responsibleAdultRequired: String? = "false",

  var parent: String? = "",

  var guardian: String? = "",

  @JsonProperty("parent_address_1")
  var parentAddress1: String? = "",

  @JsonProperty("parent_address_2")
  var parentAddress2: String? = "",

  @JsonProperty("parent_address_3")
  var parentAddress3: String? = "",

  @JsonProperty("parent_address_4")
  var parentAddress4: String? = "",

  @JsonProperty("parent_address_post_code")
  var parentPostCode: String? = "",

  @JsonProperty("parent_phone_number")
  var parentPhoneNumber: String? = "",

  @JsonProperty("parent_dob")
  var parentDateOfBirth: String? = "",

  @JsonProperty("pnc_id")
  var pncId: String? = "",

  @JsonProperty("nomis_id")
  var nomisId: String? = "",

  @JsonProperty("delius_id")
  var deliusId: String? = "",

  @JsonProperty("prison_number")
  var prisonNumber: String? = "",

  @JsonProperty("home_office_case_reference_number")
  var homeOfficeReferenceNumber: String? = "",

  @JsonProperty("cepr")
  var complianceAndEnforcementPersonReference: String? = "",

  @JsonProperty("interpreter_required")
  var interpreterRequired: String? = "",

  var language: String? = "",
) {

  companion object {
    private val log = LoggerFactory.getLogger(DeviceWearer::class.java)

    fun fromCemoOrder(order: Order, featureFlags: FeatureFlags, orderSource: FmsOrderSource): DeviceWearer {
      var adultChild = "adult"
      if (!order.deviceWearer?.adultAtTimeOfInstallation!!) {
        adultChild = "child"
      }

      var disabilities = emptyList<FmsDisability>()
      if (!order.deviceWearer?.disabilities.isNullOrBlank()) {
        disabilities = DisabilityEnum.getValuesFromEnumString(order.deviceWearer!!.disabilities!!)
          .map { disability -> FmsDisability(disability) }
      }

      val deviceWearer = DeviceWearer(
        firstName = order.deviceWearer?.firstName,
        middleName = order.deviceWearer?.middleName ?: "",
        lastName = order.deviceWearer?.lastName,
        alias = order.deviceWearer?.alias,
        dateOfBirth = order.deviceWearer?.dateOfBirth?.format(FmsDates.dateFormatter) ?: "",
        adultChild = adultChild,
        sex = getSex(order),
        genderIdentity = getGender(order),
        disability = disabilities,
        phoneNumber = getPhoneNumber(order),
        riskDetails = getRiskDetails(order),
        riskCategory = getRiskCategories(order, featureFlags),
        mappa = order.mappa?.level?.value,
        mappaCaseType = order.mappa?.category?.value,
        responsibleAdultRequired = (order.deviceWearerResponsibleAdult != null).toString(),
        parent = order.deviceWearerResponsibleAdult?.fullName ?: "",
        parentPhoneNumber = getParentPhoneNumber(order),
        interpreterRequired = order.deviceWearer?.interpreterRequired?.toString(),
        language = order.deviceWearer?.language,
        nomisId = order.deviceWearer?.nomisId,
        pncId = order.deviceWearer?.pncId,
        deliusId = order.deviceWearer?.deliusId,
        prisonNumber = order.deviceWearer?.prisonNumber,
        homeOfficeReferenceNumber = "",
      )

      if (featureFlags.ddV6CourtMappings) {
        deviceWearer.mappaCaseType = null
        deviceWearer.mappaCategory = order.mappa?.category?.value
      }

      if (featureFlags.ddV6CourtMappings) {
        deviceWearer.complianceAndEnforcementPersonReference =
          order.deviceWearer?.complianceAndEnforcementPersonReference ?: ""
      } else {
        deviceWearer.homeOfficeReferenceNumber =
          if (!order.deviceWearer?.complianceAndEnforcementPersonReference.isNullOrBlank()) {
            order.deviceWearer?.complianceAndEnforcementPersonReference
          } else {
            order.deviceWearer?.homeOfficeReferenceNumber
          }
      }

      if (order.deviceWearer?.noFixedAbode != null && !order.deviceWearer?.noFixedAbode!!) {
        val primaryAddress = order.addresses.find { address -> address.addressType == AddressType.PRIMARY }!!
        deviceWearer.address1 = primaryAddress.addressLine1
        deviceWearer.address2 =
          if (primaryAddress.addressLine2 == "" &&
            orderSource == FmsOrderSource.CEMO
          ) {
            "N/A"
          } else {
            primaryAddress.addressLine2
          }
        deviceWearer.address3 = primaryAddress.addressLine3
        deviceWearer.address4 =
          if (primaryAddress.addressLine4 == "" &&
            orderSource == FmsOrderSource.CEMO
          ) {
            "N/A"
          } else {
            primaryAddress.addressLine4
          }
        deviceWearer.addressPostCode = primaryAddress.postcode
      } else if (order.deviceWearer?.noFixedAbode == true) {
        deviceWearer.noFixedAddress = "true"
      }

      order.addresses.firstOrNull { it.addressType == AddressType.SECONDARY }?.let {
        deviceWearer.secondaryAddress1 = it.addressLine1
        deviceWearer.secondaryAddress2 = if (it.addressLine2 == "") "N/A" else it.addressLine2
        deviceWearer.secondaryAddress3 = it.addressLine3
        deviceWearer.secondaryAddress4 = if (it.addressLine4 == "") "N/A" else it.addressLine4
        deviceWearer.secondaryAddressPostCode = it.postcode
      }

      order.addresses.firstOrNull { it.addressType == AddressType.TERTIARY }?.let {
        deviceWearer.tertiaryAddress1 = it.addressLine1
        deviceWearer.tertiaryAddress2 = if (it.addressLine2 == "") "N/A" else it.addressLine2
        deviceWearer.tertiaryAddress3 = it.addressLine3
        deviceWearer.tertiaryAddress4 = if (it.addressLine4 == "") "N/A" else it.addressLine4
        deviceWearer.tertiaryAddressPostCode = it.postcode
      }

      return deviceWearer
    }

    private fun getPhoneNumber(order: Order): String? {
      if (order.contactDetails?.contactNumber == null) {
        return null
      }
      return PhoneNumberFormatter.formatAsInternationalDirectDialingNumber(order.contactDetails!!.contactNumber!!)
    }

    private fun getParentPhoneNumber(order: Order): String? {
      if (order.deviceWearerResponsibleAdult?.contactNumber == null) {
        return null
      }

      return PhoneNumberFormatter.formatAsInternationalDirectDialingNumber(
        order.deviceWearerResponsibleAdult!!.contactNumber!!,
      )
    }

    private fun getSex(order: Order): String {
      val sex = Sex.from(order.deviceWearer?.sex)

      if (sex == Sex.UNKNOWN) {
        return Sex.PREFER_NOT_TO_SAY.value
      }

      return sex?.value ?: order.deviceWearer?.sex ?: ""
    }

    private fun getGender(order: Order): String =
      Gender.from(order.deviceWearer?.gender)?.value ?: order.deviceWearer?.gender ?: ""

    private fun getRiskDetails(order: Order): String? =
      if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6)
      ) {
        val genderRisk = order.detailsOfInstallation?.genderRiskDetails?.takeIf { it.isNotBlank() }
        val riskDetails = order.detailsOfInstallation?.riskDetails ?: order.installationAndRisk?.riskDetails
        genderRisk?.let { gender ->
          "Risk to gender: $gender" +
            (
              riskDetails?.takeIf { details ->
                details.isNotBlank()
              }?.let { details -> " Additional risk details: $details" }
                ?: ""
              )
        }
          ?: riskDetails
      } else {
        order.installationAndRisk?.riskDetails
      }

    private fun getRiskCategories(order: Order, featureFlags: FeatureFlags): List<FmsRiskCategory> {
      val riskCategories =
        if (order.dataDictionaryVersion.isLaterThanOrEqual(DataDictionaryVersion.DDV6) &&
          featureFlags.ddV6CourtMappings
        ) {
          order.detailsOfInstallation?.riskCategory ?: order.installationAndRisk?.riskCategory
        } else {
          order.installationAndRisk?.riskCategory
        }

      if (riskCategories?.any() == true) {
        return riskCategories
          .filter {
            RiskCategory.entries.any { riskCategory ->
              riskCategory != RiskCategory.NO_RISK &&
                riskCategory.name == it
            }
          }
          .map { FmsRiskCategory(RiskCategory.entries.first { riskCategory -> riskCategory.name == it }.value) }
          .toList()
      }
      return emptyList()
    }
  }

  fun toDeviceWearer(versionId: UUID): CemoDeviceWearer {
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

  fun toAddress(versionId: UUID): Address? {
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

  fun toSecondaryAddress(versionId: UUID): Address? {
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

  fun toTertiaryAddress(versionId: UUID): Address? {
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

  fun toMappa(versionId: UUID): Mappa {
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

  fun toInstallationAndRisk(versionId: UUID): InstallationAndRisk {
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

  fun toResponsibleAdult(versionId: UUID): ResponsibleAdult? {
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

  private fun disabilityList(): String? {
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
}
