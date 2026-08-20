package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Alias
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.CodeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Contact
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Identifiers
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.PrisonerDetails
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class PrisonerDetailsTest {
  val basicAddress = Address(
    noFixedAbode = null,
    postcode = null,
    status = null,
    buildingNumber = null,
    buildingName = null,
    subBuildingName = null,
    postTown = null,
    county = null,
    thoroughfareName = null,
    contacts = emptyList(),
  )
  val details = PrisonerDetails(
    firstName = "Bob",
    middleNames = "Middle",
    lastName = "Builder",
    dateOfBirth = "1990-08-21",
    sex = CodeDescription(code = "M", description = "Male"),
    identifiers = Identifiers(
      crns = listOf("B123435"),
      prisonNumbers = listOf("A1234BC"),
      pncs = listOf("2000/1234567A"),
      nationalInsuranceNumbers = listOf("QQ123456B"),
    ),
    aliases = listOf(Alias(firstName = "Wendy", middleNames = "Scoop", lastName = "Dizzy")),
    addresses = listOf(basicAddress.copy(noFixedAbode = false), basicAddress.copy(noFixedAbode = true)),
    religion = CodeDescription(code = "CHRS", description = "Christianity"),
    ethnicity = CodeDescription(code = "B9", description = "Black/Black British : Any other backgr'nd"),
    nationalities = listOf(CodeDescription(code = "GB", description = "GB")),
  )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `maps to device wearer`() {
    val result: DeviceWearer = details.toDeviceWearer(versionId)
    assertThat(result.versionId).isEqualTo(versionId)
    assertThat(result.firstName).isEqualTo("Bob")
    assertThat(result.middleName).isEqualTo("Middle")
    assertThat(result.lastName).isEqualTo("Builder")
    assertThat(result.prisonNumber).isEqualTo("A1234BC")
    assertThat(result.courtCaseReferenceNumber).isEqualTo("B123435")
    assertThat(result.pncId).isEqualTo("2000/1234567A")
    assertThat(result.dateOfBirth).isEqualTo(
      ZonedDateTime.of(
        LocalDateTime.of(1990, 8, 21, 0, 0),
        ZoneId.of("Europe/London"),
      ),
    )
    assertThat(result.sex).isEqualTo("MALE")
    assertThat(result.alias).isEqualTo("Wendy Scoop Dizzy")
    assertThat(result.noFixedAbode).isEqualTo(true)
    assertThat(result.religion).isEqualTo("Christianity")
    assertThat(result.ethnicity).isEqualTo("Black/Black British : Any other backgr'nd")
    assertThat(result.nationality).isEqualTo("GB")
  }

  @Test
  fun `maps to primary address`() {
    val addressDetails = details.copy(
      addresses = listOf(
        Address(
          noFixedAbode = false,
          postcode = "AB12CD",
          status = CodeDescription(code = "M", description = ""),
          buildingNumber = "10",
          buildingName = null,
          subBuildingName = "",
          postTown = "LONDON",
          county = "COUNTY",
          thoroughfareName = "DOWNING STREET",
          contacts = emptyList(),
        ),
      ),
    )

    val address = addressDetails.toPrimaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address?.postcode).isEqualTo("AB12CD")
    assertThat(address?.addressType).isEqualTo(AddressType.PRIMARY)
    assertThat(address?.addressLine1).isEqualTo("10 Downing Street")
    assertThat(address?.addressLine2).isEqualTo("")
    assertThat(address?.addressLine3).isEqualTo("London")
    assertThat(address?.addressLine4).isEqualTo("County")
  }

  @Test
  fun `maps to secondary address`() {
    val addressDetails = details.copy(
      addresses = listOf(
        Address(
          noFixedAbode = false,
          postcode = "AB12CD",
          status = CodeDescription(code = "S", description = ""),
          buildingNumber = "10",
          buildingName = null,
          subBuildingName = "",
          postTown = "LONDON",
          county = "COUNTY",
          thoroughfareName = "DOWNING STREET",
          contacts = emptyList(),
        ),
      ),
    )

    val address = addressDetails.toSecondaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address?.postcode).isEqualTo("AB12CD")
    assertThat(address?.addressType).isEqualTo(AddressType.SECONDARY)
    assertThat(address?.addressLine1).isEqualTo("10 Downing Street")
    assertThat(address?.addressLine2).isEqualTo("")
    assertThat(address?.addressLine3).isEqualTo("London")
    assertThat(address?.addressLine4).isEqualTo("County")
  }

  @Test
  fun `maps to contact details`() {
    val contactDetails = details.copy(
      addresses = listOf(
        basicAddress.copy(
          contacts = listOf(
            Contact(
              type = CodeDescription(code = "HOME", description = "Home"),
              value = "01234567890",
            ),
          ),
        ),
      ),
    )

    val result = contactDetails.toContactDetails(versionId)

    assertThat(result?.contactNumber).isEqualTo("01234567890")
    assertThat(result?.phoneNumberAvailable).isEqualTo(true)
  }
}
