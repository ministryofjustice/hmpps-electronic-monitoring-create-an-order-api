package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.up3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
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
      noFixedAddress = "false",
      address1 = "1 fake street",
      address2 = "",
      address3 = "town",
      address4 = "",
      addressPostCode = "AB1 2CD",
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
}
