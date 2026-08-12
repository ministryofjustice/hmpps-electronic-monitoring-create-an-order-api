package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toDeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toMappa
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toSecondaryAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.toTertiaryAddress
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class DeviceWearerReverseMappingTest {

  val deviceWearerDto =
    DeviceWearer(
      title = "",
      firstName = "Bob",
      middleName = "Middleton",
      lastName = "Smith",
      alias = "Robert",
      dateOfBirth = "2000-07-01",
      sex = "Male",
      genderIdentity = "Male",
      language = "English",
      interpreterRequired = "false",
      adultChild = "adult",
      pncId = "pnc",
      nomisId = "nomis",
      deliusId = "delius",
      prisonNumber = "prisonNumber",
      homeOfficeReferenceNumber = "hocrn",
      complianceAndEnforcementPersonReference = "cepr",
      noFixedAddress = "false",
      address1 = "1 fake street",
      address2 = "",
      address3 = "town",
      address4 = "",
      addressPostCode = "AB1 2CD",
      secondaryAddress1 = "",
      secondaryAddress2 = "",
      secondaryAddress3 = "",
      secondaryAddress4 = "",
      secondaryAddressPostCode = "",
      tertiaryAddress1 = "",
      tertiaryAddress2 = "",
      tertiaryAddress3 = "",
      tertiaryAddress4 = "",
      tertiaryAddressPostCode = "",
      phoneNumber = "",
      mappa = "",
      mappaCaseType = "",
      mappaCategory = "",
      riskDetails = "",
      riskCategory = emptyList(),
      parent = "",
      guardian = "",
      parentPhoneNumber = "",
      disability = emptyList(),
    )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `it should map name fields onto cemo device wearer`() {
    val deviceWearer = deviceWearerDto.toDeviceWearer(versionId)

    assertThat(deviceWearer.versionId).isEqualTo(versionId)
    assertThat(deviceWearer.firstName).isEqualTo(deviceWearerDto.firstName)
    assertThat(deviceWearer.middleName).isEqualTo(deviceWearerDto.middleName)
    assertThat(deviceWearer.lastName).isEqualTo(deviceWearerDto.lastName)
    assertThat(deviceWearer.alias).isEqualTo(deviceWearerDto.alias)
  }

  @Test
  fun `it should map dob fields onto cemo device wearer`() {
    val deviceWearer = deviceWearerDto.toDeviceWearer(versionId)
    val expectedDOB = ZonedDateTime.of(
      2000,
      7,
      1,
      0,
      0,
      0,
      0,
      ZoneId.of("Europe/London"),
    )

    assertThat(deviceWearer.dateOfBirth).isEqualTo(expectedDOB)
  }

  @Test
  fun `it should map dw characteristics to cemo device wearer`() {
    val deviceWearer = deviceWearerDto.toDeviceWearer(versionId)

    assertThat(deviceWearer.adultAtTimeOfInstallation).isEqualTo(true)
    assertThat(deviceWearer.sex).isEqualTo("MALE")
    assertThat(deviceWearer.gender).isEqualTo("MALE")
    assertThat(deviceWearer.language).isEqualTo(deviceWearerDto.language)
    assertThat(deviceWearer.interpreterRequired).isEqualTo(false)
  }

  @Test
  fun `should map ids to cemo device wearer`() {
    val deviceWearer = deviceWearerDto.toDeviceWearer(versionId)

    assertThat(deviceWearer.pncId).isEqualTo(deviceWearerDto.pncId)
    assertThat(deviceWearer.nomisId).isEqualTo(deviceWearerDto.nomisId)
    assertThat(deviceWearer.deliusId).isEqualTo(deviceWearerDto.deliusId)
    assertThat(deviceWearer.prisonNumber).isEqualTo(deviceWearerDto.prisonNumber)
    assertThat(deviceWearer.complianceAndEnforcementPersonReference)
      .isEqualTo(deviceWearerDto.complianceAndEnforcementPersonReference)
    assertThat(
      deviceWearer.homeOfficeReferenceNumber,
    ).isEqualTo(deviceWearerDto.homeOfficeReferenceNumber)
  }

  @Test
  fun `it should map a fixed address to a cemo address`() {
    val address = deviceWearerDto.toAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.PRIMARY)
    assertThat(address.addressLine1).isEqualTo("1 fake street")
    assertThat(address.postcode).isEqualTo("AB1 2CD")
  }

  @Test
  fun `it should not build an adress when no fixed address is true`() {
    val dto = deviceWearerDto.copy(noFixedAddress = "true")

    assertThat(dto.toAddress(versionId)).isNull()
  }

  @Test
  fun `it should map a secondary to a cemo address`() {
    val dto = deviceWearerDto.copy(
      secondaryAddress1 = "2 fake street",
      secondaryAddress2 = "",
      secondaryAddress3 = "town",
      secondaryAddress4 = "",
      secondaryAddressPostCode = "AB2 3CD",
    )
    val address = dto.toSecondaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.SECONDARY)
    assertThat(address.addressLine1).isEqualTo("2 fake street")
    assertThat(address.postcode).isEqualTo("AB2 3CD")
  }

  @Test
  fun `it should not build a secondary when empty`() {
    assertThat(deviceWearerDto.toSecondaryAddress(versionId)).isNull()
  }

  @Test
  fun `it should map a tiertiary to a cemo address`() {
    val dto = deviceWearerDto.copy(
      tertiaryAddress1 = "3 fake street",
      tertiaryAddress2 = "",
      tertiaryAddress3 = "town",
      tertiaryAddress4 = "",
      tertiaryAddressPostCode = "AB3 4CD",
    )
    val address = dto.toTertiaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.TERTIARY)
    assertThat(address.addressLine1).isEqualTo("3 fake street")
    assertThat(address.postcode).isEqualTo("AB3 4CD")
  }

  @Test
  fun `it should not build a tiertiary when empty`() {
    assertThat(deviceWearerDto.toTertiaryAddress(versionId)).isNull()
  }

  @Test
  fun `it should map mappa level and category onto cemo mappa`() {
    val dto = deviceWearerDto.copy(mappa = "MAPPA 2", mappaCategory = "Category 1")
    val mappa = dto.toMappa(versionId)

    assertThat(mappa.versionId).isEqualTo(versionId)
    assertThat(mappa.level).isEqualTo(MappaLevel.MAPPA_TWO)
    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `it should prefer mappa_category over mappa_case_type when both present`() {
    val dto = deviceWearerDto.copy(mappaCaseType = "Category 3", mappaCategory = "Category 1")
    val mappa = dto.toMappa(versionId)

    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `it should fall back to mappa_case_type when mappa_category is blank`() {
    val dto = deviceWearerDto.copy(mappaCaseType = "Category 3", mappaCategory = "")
    val mappa = dto.toMappa(versionId)

    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_THREE)
  }

  @Test
  fun `it should leave level and category null and not throw when values are unmatched`() {
    val dto = deviceWearerDto.copy(mappa = "not a real level", mappaCategory = "not a real category")
    val mappa = dto.toMappa(versionId)

    assertThat(mappa.level).isNull()
    assertThat(mappa.category).isNull()
  }

  @Test
  fun `it should mark isMappa as YES when level or category is present`() {
    val dto = deviceWearerDto.copy(mappa = "MAPPA 1", mappaCategory = "")
    val mappa = dto.toMappa(versionId)

    assertThat(mappa.isMappa).isEqualTo(YesNoUnknown.YES)
  }

  @Test
  fun `it should set isMappa to unknown when neither level nor category is present`() {
    val mappa = deviceWearerDto.toMappa(versionId)

    assertThat(mappa.isMappa).isEqualTo(YesNoUnknown.UNKNOWN)
  }

  @Test
  fun `it should map parent onto cemo responsible adult as Parent`() {
    val dto = deviceWearerDto.copy(parent = "Jane Smith")
    val responsibleAdult = dto.toResponsibleAdult(versionId)

    assertThat(responsibleAdult).isNotNull
    assertThat(responsibleAdult!!.versionId).isEqualTo(versionId)
    assertThat(responsibleAdult.fullName).isEqualTo("Jane Smith")
    assertThat(responsibleAdult.relationship).isEqualTo("Parent")
  }

  @Test
  fun `it should map guardian onto cemo responsible adult as Guardian when no parent`() {
    val dto = deviceWearerDto.copy(guardian = "Jane Guardian")
    val responsibleAdult = dto.toResponsibleAdult(versionId)

    assertThat(responsibleAdult).isNotNull
    assertThat(responsibleAdult!!.fullName).isEqualTo("Jane Guardian")
    assertThat(responsibleAdult.relationship).isEqualTo("Guardian")
  }

  @Test
  fun `it should prefer parent over guardian when both present`() {
    val dto = deviceWearerDto.copy(parent = "Jane Smith", guardian = "Jane Guardian")
    val responsibleAdult = dto.toResponsibleAdult(versionId)

    assertThat(responsibleAdult!!.fullName).isEqualTo("Jane Smith")
    assertThat(responsibleAdult.relationship).isEqualTo("Parent")
  }

  @Test
  fun `it should map parent phone number onto cemo responsible adult contact number`() {
    val dto = deviceWearerDto.copy(parent = "Jane Smith", parentPhoneNumber = "07000000000")
    val responsibleAdult = dto.toResponsibleAdult(versionId)

    assertThat(responsibleAdult!!.contactNumber).isEqualTo("07000000000")
  }

  @Test
  fun `it should not build a responsible adult when neither parent nor guardian is present`() {
    assertThat(deviceWearerDto.toResponsibleAdult(versionId)).isNull()
  }

  @Test
  fun `it should map disabilities onto cemo device wearer as a joined enum-name string`() {
    val dto = deviceWearerDto.copy(
      disability = listOf(Disability("Vision"), Disability("Hearing")),
    )
    val deviceWearer = dto.toDeviceWearer(versionId)

    assertThat(deviceWearer.disabilities).isEqualTo("VISION,HEARING")
  }

  @Test
  fun `it should drop unmatched disabilities and not throw`() {
    val dto = deviceWearerDto.copy(
      disability = listOf(Disability("Vision"), Disability("not a real disability")),
    )
    val deviceWearer = dto.toDeviceWearer(versionId)

    assertThat(deviceWearer.disabilities).isEqualTo("VISION")
  }

  @Test
  fun `it should leave disabilities null when none are present`() {
    val deviceWearer = deviceWearerDto.toDeviceWearer(versionId)

    assertThat(deviceWearer.disabilities).isNull()
  }

  @Test
  fun `it should map phone number to contact details`() {
    val dto = deviceWearerDto.copy(phoneNumber = "01234567890")
    val contactDetails = dto.toContactDetails(versionId)

    assertThat(contactDetails).isNotNull()
    assertThat(contactDetails.contactNumber).isEqualTo("01234567890")
    assertThat(contactDetails.phoneNumberAvailable).isEqualTo(true)
  }

  @Test
  fun `it should not map phone number to contact details when blank or null`() {
    val contactDetails = deviceWearerDto.toContactDetails(versionId)

    assertThat(contactDetails).isNotNull()
    assertThat(contactDetails.contactNumber).isNull()
    assertThat(contactDetails.phoneNumberAvailable).isEqualTo(false)
  }
}
