package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRiskCategory
import java.util.stream.Stream

data class FieldChangeCase(val name: String, val mutate: (DeviceWearer) -> Unit, val expectedMessage: String? = null) {
  override fun toString(): String = name
}

data class OVTChangeCase(val name: String, val mutate: (DeviceWearer) -> Unit, val expected: VariationType? = null) {
  override fun toString(): String = name
}

class DeviceWearerFieldChangeArgumentsProvider : ArgumentsProvider {

  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?> {
    val cases = listOf(

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.firstName = "Jane" },
        expectedMessage = "Device wearer's name has changed",
      ),

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.middleName = "Changed" },
        expectedMessage = "Device wearer's name has changed",
      ),

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.lastName = "Changed" },
        expectedMessage = "Device wearer's name has changed",
      ),

      FieldChangeCase(
        name = "alias",
        mutate = { it.alias = "New Alias" },
        expectedMessage = "Device wearer's preferred name has changed",
      ),

      FieldChangeCase(
        name = "dateOfBirth",
        mutate = { it.dateOfBirth = "2000-01-01" },
        expectedMessage = "Device wearer's date of birth has changed",
      ),

      FieldChangeCase(
        name = "sex",
        mutate = { it.sex = "Female" },
        expectedMessage = "Device wearer's sex has changed",
      ),

      FieldChangeCase(
        name = "genderIdentity",
        mutate = { it.genderIdentity = "Other" },
        expectedMessage = "Device wearer's gender has changed",
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address1 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed",
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address2 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed",
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address3 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed",
      ),
      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address4 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed",
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.addressPostCode = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed",
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress1 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed",
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress2 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed",
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress3 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed",
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress4 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed",
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddressPostCode = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed",
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress1 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed",
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress2 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed",
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress3 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed",
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress4 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed",
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddressPostCode = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed",
      ),

      FieldChangeCase(
        name = "phoneNumber",
        mutate = { it.phoneNumber = "07999999999" },
        expectedMessage = "Device wearer's phone number has changed",
      ),

      FieldChangeCase(
        name = "mappa",
        mutate = { it.mappa = "No" },
        expectedMessage = "Device wearer's MAPPA level has changed",
      ),

      FieldChangeCase(
        name = "mappaCaseType",
        mutate = { it.mappaCaseType = "No" },
        expectedMessage = "Device wearer's MAPPA case type has changed",
      ),

      FieldChangeCase(
        name = "mappaCategory",
        mutate = { it.mappaCategory = "No" },
        expectedMessage = "Device wearer's MAPPA category has changed",
      ),

      FieldChangeCase(
        name = "disability",
        mutate = { it.disability = listOf(Disability("Hearing")) },
        expectedMessage = "Device wearer's disability or health conditions have changed",
      ),

      FieldChangeCase(
        name = "riskCategory",
        mutate = {
          it.riskCategory = listOf(FmsRiskCategory("Low"))
        },
        expectedMessage = "Device wearer's risk categories have changed",
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.pncId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed",
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.nomisId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed",
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.deliusId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed",
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.prisonNumber = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed",
      ),

      FieldChangeCase(
        name = "cepr",
        mutate = {
          it.complianceAndEnforcementPersonReference = "CEPR456"
        },
        expectedMessage = "Device wearer's personal ID number(s) have changed",
      ),

      FieldChangeCase(
        name = "interpreterRequired",
        mutate = { it.interpreterRequired = "Yes" },
        expectedMessage = "Device wearer's interpreter needs have changed",
      ),

      FieldChangeCase(
        name = "interpreterRequired",
        mutate = { it.language = "GK" },
        expectedMessage = "Device wearer's interpreter needs have changed",
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.responsibleAdultRequired = "true" },
        expectedMessage = "Responsible adult's details have changed",
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.parent = "abc" },
        expectedMessage = "Responsible adult's details have changed",
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.guardian = "acb" },
        expectedMessage = "Responsible adult's details have changed",
      ),

      FieldChangeCase(
        name = "parentPhoneNumber",
        mutate = { it.parentPhoneNumber = "07123456789" },
        expectedMessage = "Responsible adult's phone number has changed",
      ),
    )

    return cases.map { Arguments.of(it) }.stream()
  }
}

class DeviceWearerNegativeFieldArgumentsProvider : ArgumentsProvider {

  override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
    val cases = listOf(
      FieldChangeCase(
        name = "title",
        mutate = { it.title = "Updated risk narrative" },
      ),

      FieldChangeCase(
        name = "homeOfficeReferenceNumber",
        mutate = { it.homeOfficeReferenceNumber = "Updated" },
      ),
      FieldChangeCase(
        name = "riskSelfHarm",
        mutate = { it.riskSelfHarm = "Updated" },
      ),
    )
    return cases
      .map { Arguments.of(it) }
      .stream()
  }
}

class DeviceWearerOrderVariationTypeArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
    val cases = listOf(
      OVTChangeCase(
        name = "first_name",
        mutate = { it.firstName = "Jane" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "middle_name",
        mutate = { it.middleName = "Jane" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "last_name",
        mutate = { it.lastName = "Jane" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "alias",
        mutate = { it.alias = "new alias" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "date_of_birth",
        mutate = { it.dateOfBirth = "2000-01-01" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "mappa",
        mutate = { it.mappa = "Level 1" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "pnc_id",
        mutate = { it.pncId = "123456A" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "interpreter_required",
        mutate = { it.interpreterRequired = "Yes" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "parent",
        mutate = { it.parent = "new parent" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "guardian",
        mutate = { it.guardian = "new guardian" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "language",
        mutate = { it.language = "new language" },
        expected = VariationType.CHANGE_TO_PERSONAL_DETAILS,
      ),
      OVTChangeCase(
        name = "address_1",
        mutate = { it.address1 = "10 Downing St" },
        expected = VariationType.CHANGE_TO_ADDRESS,
      ),
      OVTChangeCase(
        name = "secondary_address_1",
        mutate = { it.secondaryAddress1 = "Flat 4" },
        expected = VariationType.CHANGE_TO_ADDRESS,
      ),
      OVTChangeCase(
        name = "tertiary_address_1",
        mutate = { it.tertiaryAddress1 = "Care Home" },
        expected = VariationType.CHANGE_TO_ADDRESS,
      ),
      OVTChangeCase(
        name = "no_fixed_address",
        mutate = { it.noFixedAddress = "true" },
        expected = VariationType.CHANGE_TO_ADDRESS,
      ),
    )
    return cases
      .map { Arguments.of(it) }
      .stream()
  }
}

class DeviceWearerNoOrderVariationTypeArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
    val negativeCases = listOf(

      OVTChangeCase("title", { it.title = "Mr" }),
      OVTChangeCase("risk_serious_harm", { it.riskSeriousHarm = "true" }),
      OVTChangeCase("risk_self_harm", { it.riskSelfHarm = "true" }),
      OVTChangeCase("risk_details", { it.riskDetails = "Details" }),
      OVTChangeCase("parent_address_1", { it.parentAddress1 = "Address 1" }),
      OVTChangeCase("parent_address_2", { it.parentAddress2 = "Address 2" }),
      OVTChangeCase("parent_address_3", { it.parentAddress3 = "Address 3" }),
      OVTChangeCase("parent_address_4", { it.parentAddress4 = "Address 4" }),
      OVTChangeCase("parent_address_post_code", { it.parentPostCode = "SW1A 4AA" }),
      OVTChangeCase("parent_dob", { it.parentDateOfBirth = "2000-01-01" }),
    )

    return negativeCases
      .map { Arguments.of(it) }
      .stream()
  }
}
