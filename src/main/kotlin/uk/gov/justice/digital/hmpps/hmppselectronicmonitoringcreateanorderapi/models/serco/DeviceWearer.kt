package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.serco

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import java.time.LocalDate
import java.time.Period
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
  var disabilities: List<Disability>? = emptyList(),
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
  var partentDateOfBirth: String? = "",
){

  companion object{
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun fromCemoOrder(order:OrderForm):DeviceWearer{

      var adultChild= "adult"
      if(!order.deviceWearer.isAdult)
        adultChild="child"
      val primaryAddress = order.deviceWearer.deviceWearerAddresses.find{address -> address.addressType==DeviceWearerAddressType.PRIMARY }!!

      return DeviceWearer(
        firstName = order.deviceWearer.firstName,
        lastName = order.deviceWearer.lastName,
        alias = order.deviceWearer.alias,
        dateOfBirth = order.deviceWearer.dateOfBirth!!.format(formatter),
        adultChild =adultChild,
        sex =  order.deviceWearer.gender,
        address1 =  primaryAddress.AddressLine1,
        address2 = primaryAddress.city,
        addressPostCode =  primaryAddress.postcode,
        phoneNumber = order.deviceWearerContactDetails.contactNumber,


        )
    }
  }
}

data class Disability(
  var disability: String? = "",
)

data class RiskCategory(
  var category: String? = "",
)