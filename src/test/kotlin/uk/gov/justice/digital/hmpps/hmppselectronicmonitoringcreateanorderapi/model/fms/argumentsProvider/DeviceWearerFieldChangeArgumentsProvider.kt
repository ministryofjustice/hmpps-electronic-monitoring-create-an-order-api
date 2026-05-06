package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRiskCategory
import java.util.stream.Stream


data class FieldChangeCase(
  val name: String,
  val mutate: (DeviceWearer) -> Unit,
  val expectedMessage: String? = null,
)
{
  override fun toString(): String = name
}


class DeviceWearerFieldChangeArgumentsProvider: ArgumentsProvider {

  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?> {
    val cases = listOf(

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.firstName = "Jane" },
        expectedMessage = "Device wearer's name has changed"
      ),

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.middleName = "Changed" },
        expectedMessage = "Device wearer's name has changed"
      ),

      FieldChangeCase(
        name = "nameChange",
        mutate = { it.lastName = "Changed" },
        expectedMessage = "Device wearer's name has changed"
      ),

      FieldChangeCase(
        name = "alias",
        mutate = { it.alias = "New Alias" },
        expectedMessage = "Device wearer's preferred name has changed"
      ),

      FieldChangeCase(
        name = "dateOfBirth",
        mutate = { it.dateOfBirth = "2000-01-01" },
        expectedMessage = "Device wearer's date of birth has changed"
      ),

      FieldChangeCase(
        name = "sex",
        mutate = { it.sex = "Female" },
        expectedMessage = "Device wearer's sex has changed"
      ),

      FieldChangeCase(
        name = "genderIdentity",
        mutate = { it.genderIdentity = "Other" },
        expectedMessage = "Device wearer's gender has changed"
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address1 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed"
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address2 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed"
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address3 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed"
      ),
      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.address4 = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed"
      ),

      FieldChangeCase(
        name = "primaryAddressChange",
        mutate = { it.addressPostCode = "456 New Road" },
        expectedMessage = "Device wearer's main address has changed"
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress1 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed"
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress2 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed"
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress3 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed"
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddress4 = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed"
      ),

      FieldChangeCase(
        name = "secondaryAddressChange",
        mutate = { it.secondaryAddressPostCode = "New Secondary" },
        expectedMessage = "Device wearer's secondary address has changed"
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress1 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed"
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress2 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed"
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress3 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed"
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddress4 = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed"
      ),

      FieldChangeCase(
        name = "tertiaryAddressChange",
        mutate = { it.tertiaryAddressPostCode = "New Tertiary" },
        expectedMessage = "Device wearer's tertiary address has changed"
      ),

      FieldChangeCase(
        name = "noFixedAddress",
        mutate = { it.noFixedAddress = "true" },
        expectedMessage = "Device wearer now has / doesn't have a fixed address"
      ),

      FieldChangeCase(
        name = "phoneNumber",
        mutate = { it.phoneNumber = "07999999999" },
        expectedMessage = "Device wearer's phone number has changed"
      ),

      FieldChangeCase(
        name = "mappa",
        mutate = { it.mappa = "No" },
        expectedMessage = "Device wearer's MAPPA has changed"
      ),

      FieldChangeCase(
        name = "mappa",
        mutate = { it.mappaCaseType = "No" },
        expectedMessage = "Device wearer's MAPPA has changed"
      ),

      FieldChangeCase(
        name = "mappa",
        mutate = { it.mappaCategory = "No" },
        expectedMessage = "Device wearer's MAPPA has changed"
      ),

      FieldChangeCase(
        name = "disability",
        mutate = { it.disability = listOf(Disability("Hearing")) },
        expectedMessage = "Device wearer's disability or health conditions have changed"
      ),

      FieldChangeCase(
        name = "riskCategory",
        mutate = {
          it.riskCategory = listOf(FmsRiskCategory("Low"))
        },
        expectedMessage = "Device wearer's risk categories have changed"
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.pncId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed"
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.nomisId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed"
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.deliusId = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed"
      ),

      FieldChangeCase(
        name = "personalIdChanged",
        mutate = { it.prisonNumber = "PNC123" },
        expectedMessage = "Device wearer's personal ID number(s) have changed"
      ),

      FieldChangeCase(
        name = "cepr",
        mutate = {
          it.complianceAndEnforcementPersonReference = "CEPR456"
        },
        expectedMessage = "Device wearer's personal ID number(s) have changed"
      ),

      FieldChangeCase(
        name = "interpreterRequired",
        mutate = { it.interpreterRequired = "Yes" },
        expectedMessage = "Device wearer's interpreter needs have changed"
      ),

      FieldChangeCase(
        name = "interpreterRequired",
        mutate = { it.language = "GK" },
        expectedMessage = "Device wearer's interpreter needs have changed"
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.responsibleAdultRequired = "true" },
        expectedMessage = "Responsible adult's details have changed"
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.parent = "abc" },
        expectedMessage = "Responsible adult's details have changed"
      ),

      FieldChangeCase(
        name = "responsibleAdultChanged",
        mutate = { it.guardian = "acb" },
        expectedMessage = "Responsible adult's details have changed"
      ),

      FieldChangeCase(
        name = "parentPhoneNumber",
        mutate = { it.parentPhoneNumber = "07123456789" },
        expectedMessage = "Responsible adult's phone number has changed"
      )
    )

    return cases.map { Arguments.of(it) }.stream()
  }

}
class DeviceWearerNegativeFieldArgumentsProvider : ArgumentsProvider {

    override fun provideArguments(
      context: ExtensionContext
    ): Stream<out Arguments> {

      val cases = listOf(
        FieldChangeCase(
          name = "title",
          mutate = { it.title = "Updated risk narrative" }
        ),

        FieldChangeCase(
          name = "homeOfficeReferenceNumber",
          mutate = { it.homeOfficeReferenceNumber = "Updated" }
        ),
        FieldChangeCase(
          name = "riskSelfHarm",
          mutate = { it.riskSelfHarm = "Updated" }
        ),
      )
      return cases
        .map { Arguments.of(it) }
        .stream()
    }
  }

