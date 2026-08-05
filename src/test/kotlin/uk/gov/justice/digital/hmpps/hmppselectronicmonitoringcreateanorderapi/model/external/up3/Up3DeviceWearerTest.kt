package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.external.up3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
      dateOfBirth = "2000-01-01 00:00:00",
      sex = "Male",
      genderIdentity = "Male",
      language = "English",
      interpreterRequired = "false",
      adultChild = "adult",
    )
  val versionId: UUID = UUID.randomUUID()

  @Test
  fun `it should map name fields onto CEMO device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.versionId).isEqualTo(versionId)
    assertThat(deviceWearer.firstName).isEqualTo(up3DeviceWearer.firstName)
    assertThat(deviceWearer.lastName).isEqualTo(up3DeviceWearer.lastName)
    assertThat(deviceWearer.alias).isEqualTo(up3DeviceWearer.alias)
  }

  @Test
  fun `it should map dob fields onto CEMO device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)
    val expectedDOB = ZonedDateTime.of(
      2000,
      1,
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
  fun `it should map dw characteristics to CEMO device wearer`() {
    val deviceWearer = up3DeviceWearer.toDeviceWearer(versionId)

    assertThat(deviceWearer.adultAtTimeOfInstallation).isEqualTo(true)
    assertThat(deviceWearer.sex).isEqualTo(up3DeviceWearer.sex)
    assertThat(deviceWearer.gender).isEqualTo(up3DeviceWearer.genderIdentity)
    assertThat(deviceWearer.language).isEqualTo(up3DeviceWearer.language)
    assertThat(deviceWearer.interpreterRequired).isEqualTo(false)
  }
}
