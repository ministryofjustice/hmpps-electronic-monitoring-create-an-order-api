package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.up3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YesNoUnknown
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Up3DeviceWearer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class Up3DeviceWearerTest {

  val up3DeviceWearer =
    Up3DeviceWearer(
      title = "",
      firstName = "Bob",
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
      homeOfficeCaseReferenceNumber = "hocrn",
      cepr = "cepr",
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
      mappa = "",
      mappaCaseType = "",
      mappaCategory = "",
    )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `it should map name fields onto cemo device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.versionId).isEqualTo(versionId)
    assertThat(deviceWearer.firstName).isEqualTo(up3DeviceWearer.firstName)
    assertThat(deviceWearer.lastName).isEqualTo(up3DeviceWearer.lastName)
    assertThat(deviceWearer.alias).isEqualTo(up3DeviceWearer.alias)
  }

  @Test
  fun `it should map dob fields onto cemo device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)
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
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.adultAtTimeOfInstallation).isEqualTo(true)
    assertThat(deviceWearer.sex).isEqualTo(up3DeviceWearer.sex)
    assertThat(deviceWearer.gender).isEqualTo(up3DeviceWearer.genderIdentity)
    assertThat(deviceWearer.language).isEqualTo(up3DeviceWearer.language)
    assertThat(deviceWearer.interpreterRequired).isEqualTo(false)
  }

  @Test
  fun `should map ids to cemo device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.pncId).isEqualTo(up3DeviceWearer.pncId)
    assertThat(deviceWearer.nomisId).isEqualTo(up3DeviceWearer.nomisId)
    assertThat(deviceWearer.deliusId).isEqualTo(up3DeviceWearer.deliusId)
    assertThat(deviceWearer.prisonNumber).isEqualTo(up3DeviceWearer.prisonNumber)
    assertThat(deviceWearer.complianceAndEnforcementPersonReference).isEqualTo(up3DeviceWearer.cepr)
    assertThat(
      deviceWearer.homeOfficeReferenceNumber,
    ).isEqualTo(up3DeviceWearer.homeOfficeCaseReferenceNumber)
  }

  @Test
  fun `it should map a fixed address to a cemo address`() {
    val address = up3DeviceWearer.toAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.PRIMARY)
    assertThat(address.addressLine1).isEqualTo("1 fake street")
    assertThat(address.postcode).isEqualTo("AB1 2CD")
  }

  @Test
  fun `it should not build an adress when no fixed address is true`() {
    val up3 = up3DeviceWearer.copy(noFixedAddress = "true")

    assertThat(up3.toAddress(versionId)).isNull()
  }

  @Test
  fun `it should map a secondary to a cemo address`() {
    val up3 = up3DeviceWearer.copy(
      secondaryAddress1 = "2 fake street",
      secondaryAddress2 = "",
      secondaryAddress3 = "town",
      secondaryAddress4 = "",
      secondaryAddressPostCode = "AB2 3CD",
    )
    val address = up3.toSecondaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.SECONDARY)
    assertThat(address.addressLine1).isEqualTo("2 fake street")
    assertThat(address.postcode).isEqualTo("AB2 3CD")
  }

  @Test
  fun `it should not build a secondary when empty`() {
    assertThat(up3DeviceWearer.toSecondaryAddress(versionId)).isNull()
  }

  @Test
  fun `it should map a tiertiary to a cemo address`() {
    val up3 = up3DeviceWearer.copy(
      tertiaryAddress1 = "3 fake street",
      tertiaryAddress2 = "",
      tertiaryAddress3 = "town",
      tertiaryAddress4 = "",
      tertiaryAddressPostCode = "AB3 4CD",
    )
    val address = up3.toTertiaryAddress(versionId)

    assertThat(address).isNotNull
    assertThat(address!!.addressType).isEqualTo(AddressType.TERTIARY)
    assertThat(address.addressLine1).isEqualTo("3 fake street")
    assertThat(address.postcode).isEqualTo("AB3 4CD")
  }

  @Test
  fun `it should not build a tiertiary when empty`() {
    assertThat(up3DeviceWearer.toTertiaryAddress(versionId)).isNull()
  }

  @Test
  fun `it should map mappa level and category onto cemo mappa`() {
    val up3 = up3DeviceWearer.copy(mappa = "MAPPA 2", mappaCategory = "Category 1")
    val mappa = up3.toMappa(versionId)

    assertThat(mappa.versionId).isEqualTo(versionId)
    assertThat(mappa.level).isEqualTo(MappaLevel.MAPPA_TWO)
    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `it should prefer mappa_category over mappa_case_type when both present`() {
    val up3 = up3DeviceWearer.copy(mappaCaseType = "Category 3", mappaCategory = "Category 1")
    val mappa = up3.toMappa(versionId)

    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_ONE)
  }

  @Test
  fun `it should fall back to mappa_case_type when mappa_category is blank`() {
    val up3 = up3DeviceWearer.copy(mappaCaseType = "Category 3", mappaCategory = "")
    val mappa = up3.toMappa(versionId)

    assertThat(mappa.category).isEqualTo(MappaCategory.CATEGORY_THREE)
  }

  @Test
  fun `it should leave level and category null and not throw when values are unmatched`() {
    val up3 = up3DeviceWearer.copy(mappa = "not a real level", mappaCategory = "not a real category")
    val mappa = up3.toMappa(versionId)

    assertThat(mappa.level).isNull()
    assertThat(mappa.category).isNull()
  }

  @Test
  fun `it should mark isMappa as YES when level or category is present`() {
    val up3 = up3DeviceWearer.copy(mappa = "MAPPA 1", mappaCategory = "")
    val mappa = up3.toMappa(versionId)

    assertThat(mappa.isMappa).isEqualTo(YesNoUnknown.YES)
  }

  @Test
  fun `it should leave isMappa unset when neither level nor category is present`() {
    val mappa = up3DeviceWearer.toMappa(versionId)

    assertThat(mappa.isMappa).isNull()
  }
}
