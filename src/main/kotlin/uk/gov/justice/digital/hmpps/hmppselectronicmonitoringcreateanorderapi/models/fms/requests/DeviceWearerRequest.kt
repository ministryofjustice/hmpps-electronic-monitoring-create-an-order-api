package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.FmsDisability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.FmsRiskCategory

open class DeviceWearerRequest {
  open val title: String = ""

  @JsonProperty("first_name")
  open val firstName: String = ""

  @JsonProperty("middle_name")
  open val middleName: String = ""

  @JsonProperty("last_name")
  open val lastName: String = ""

  open val alias: String? = null

  @JsonProperty("date_of_birth")
  open val dateOfBirth: String = ""

  @JsonProperty("adult_child")
  open val adultChild: String = ""

  open val sex: String = ""

  @JsonProperty("gender_identity")
  open val genderIdentity: String = ""

  open val disability: List<FmsDisability> = emptyList()

  @JsonProperty("address_1")
  open val address1: String = ""

  @JsonProperty("address_2")
  open val address2: String = ""

  @JsonProperty("address_3")
  open val address3: String = ""

  @JsonProperty("address_4")
  open val address4: String = ""

  @JsonProperty("address_post_code")
  open val addressPostCode: String = ""

  @JsonProperty("secondary_address_1")
  open val secondaryAddress1: String = ""

  @JsonProperty("secondary_address_2")
  open val secondaryAddress2: String = ""

  @JsonProperty("secondary_address_3")
  open val secondaryAddress3: String = ""

  @JsonProperty("secondary_address_4")
  open val secondaryAddress4: String = ""

  @JsonProperty("secondary_address_post_code")
  open val secondaryAddressPostCode: String = ""

  @JsonProperty("phone_number")
  open val phoneNumber: String = ""

  @JsonProperty("risk_serious_harm")
  open val riskSeriousHarm: String = ""

  @JsonProperty("risk_self_harm")
  open val riskSelfHarm: String = ""

  @JsonProperty("risk_details")
  open val riskDetails: String? = null

  open val mappa: String? = null

  @JsonProperty("mappa_case_type")
  open val mappaCaseType: String? = null

  @JsonProperty("risk_categories")
  open val riskCategory: List<FmsRiskCategory> = emptyList()

  @JsonProperty("responsible_adult_required")
  open val responsibleAdultRequired: String = "false"

  open val parent: String = ""

  open val guardian: String = ""

  @JsonProperty("parent_address_1")
  open val parentAddress1: String = ""

  @JsonProperty("parent_address_2")
  open val parentAddress2: String = ""

  @JsonProperty("parent_address_3")
  open val parentAddress3: String = ""

  @JsonProperty("parent_address_4")
  open val parentAddress4: String = ""

  @JsonProperty("parent_address_post_code")
  open val parentPostCode: String = ""

  @JsonProperty("parent_phone_number")
  open val parentPhoneNumber: String? = null

  @JsonProperty("parent_dob")
  open val parentDateOfBirth: String = ""

  @JsonProperty("pnc_id")
  open val pncId: String? = null

  @JsonProperty("nomis_id")
  open val nomisId: String? = null

  @JsonProperty("delius_id")
  open val deliusId: String? = null

  @JsonProperty("prison_number")
  open val prisonNumber: String? = null

  @JsonProperty("home_office_case_reference_number")
  open val homeOfficeReferenceNumber: String? = null

  @JsonProperty("interpreter_required")
  open val interpreterRequired: String? = null

  open val language: String? = null
}
