package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import java.time.format.DateTimeFormatter

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

  var disability: List<Disability>? = emptyList(),

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

      val primaryAddress = order.addresses.find { address -> address.addressType == AddressType.PRIMARY }!!
      val disabilities = order.deviceWearer?.disabilities?.split(
        ',',
      )?.map { disability -> Disability(disability) }?.toList()
      val deviceWearer = DeviceWearer(
        firstName = order.deviceWearer?.firstName,
        lastName = order.deviceWearer?.lastName,
        alias = order.deviceWearer?.alias,
        dateOfBirth = order.deviceWearer?.dateOfBirth!!.format(formatter),
        adultChild = adultChild,
        sex = order.deviceWearer?.sex,
        genderIdentity = order.deviceWearer?.gender,
        disability = disabilities,
        address1 = primaryAddress.addressLine1,
        address2 = primaryAddress.addressLine2,
        address3 = primaryAddress.addressLine3,
        address4 = primaryAddress.addressLine4,
        addressPostCode = primaryAddress.postcode,
        phoneNumber = order.deviceWearerContactDetails?.contactNumber,
        riskSeriousHarm = order.installationAndRisk?.riskOfSeriousHarm,
        riskSelfHarm = order.installationAndRisk?.riskOfSelfHarm,
        riskDetails = order.installationAndRisk?.riskDetails,
        mappa = order.installationAndRisk?.mappaLevel,
        mappaCaseType = order.installationAndRisk?.mappaCaseType,
        responsibleAdultRequired = (order.deviceWearerResponsibleAdult != null).toString(),
        parent = "${order.deviceWearerResponsibleAdult?.fullName}",
        parentPhoneNumber = order.deviceWearerResponsibleAdult?.contactNumber,
        interpreterRequired = order.deviceWearer?.interpreterRequired!!.toString(),
        language = order.deviceWearer?.language,
        nomisId = order.deviceWearer?.nomisId,
        pncId = order.deviceWearer?.pncId,
        deliusId = order.deviceWearer?.deliusId,
        homeOfficeReferenceNumber = order.deviceWearer?.homeOfficeReferenceNumber,
        prisonNumber = order.deviceWearer?.prisonNumber,
      )
      order.addresses.find { address -> address.addressType == AddressType.SECONDARY }.let { address ->
        {
          if (address != null) {
            deviceWearer.secondaryAddress1 = address.addressLine1
            deviceWearer.secondaryAddress2 = address.addressLine2
            deviceWearer.secondaryAddress3 = address.addressLine3
            deviceWearer.secondaryAddress4 = address.addressLine4
            deviceWearer.secondaryAddressPostCode = address.postcode
          }
        }
      }

      return deviceWearer
    }
  }
}

data class Disability(
  var disability: String? = "",
)

data class RiskCategory(
  var category: String? = "",
)
