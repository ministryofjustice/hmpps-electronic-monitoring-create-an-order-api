package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Sex
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.formatters.PhoneNumberFormatter
import java.time.format.DateTimeFormatter
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

  @JsonProperty("risk_categories")
  var riskCategory: List<RiskCategory>? = emptyList(),

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

  @JsonProperty("interpreter_required")
  var interpreterRequired: String? = "",

  var language: String? = "",
) {

  companion object {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun fromCemoOrder(order: Order): DeviceWearer {
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
        lastName = order.deviceWearer?.lastName,
        alias = order.deviceWearer?.alias,
        dateOfBirth = order.deviceWearer?.dateOfBirth?.format(formatter) ?: "",
        adultChild = adultChild,
        sex = getSex(order),
        genderIdentity = order.deviceWearer?.gender ?: "",
        disability = disabilities,
        phoneNumber = getPhoneNumber(order),
        riskDetails = order.installationAndRisk?.riskDetails,
        mappa = order.installationAndRisk?.mappaLevel,
        mappaCaseType = order.installationAndRisk?.mappaCaseType,
        responsibleAdultRequired = (order.deviceWearerResponsibleAdult != null).toString(),
        parent = order.deviceWearerResponsibleAdult?.fullName ?: "",
        parentPhoneNumber = getParentPhoneNumber(order),
        interpreterRequired = order.deviceWearer?.interpreterRequired?.toString(),
        language = order.deviceWearer?.language,
        nomisId = order.deviceWearer?.nomisId,
        pncId = order.deviceWearer?.pncId,
        deliusId = order.deviceWearer?.deliusId,
        homeOfficeReferenceNumber = order.deviceWearer?.homeOfficeReferenceNumber,
        prisonNumber = order.deviceWearer?.prisonNumber,
      )

      if (order.deviceWearer?.noFixedAbode != null && !order.deviceWearer?.noFixedAbode!!) {
        val primaryAddress = order.addresses.find { address -> address.addressType == AddressType.PRIMARY }!!
        deviceWearer.address1 = primaryAddress.addressLine1
        deviceWearer.address2 = if (primaryAddress.addressLine2 == "") "N/A" else primaryAddress.addressLine2
        deviceWearer.address3 = primaryAddress.addressLine3
        deviceWearer.address4 = if (primaryAddress.addressLine4 == "") "N/A" else primaryAddress.addressLine4
        deviceWearer.addressPostCode = primaryAddress.postcode
      }

      order.addresses.firstOrNull { it.addressType == AddressType.SECONDARY }?.let {
        deviceWearer.secondaryAddress1 = it.addressLine1
        deviceWearer.secondaryAddress2 = if (it.addressLine2 == "") "N/A" else it.addressLine2
        deviceWearer.secondaryAddress3 = it.addressLine3
        deviceWearer.secondaryAddress4 = if (it.addressLine4 == "") "N/A" else it.addressLine4
        deviceWearer.secondaryAddressPostCode = it.postcode
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
  }
}

data class Disability(var disability: String? = "")

data class RiskCategory(var category: String? = "")
