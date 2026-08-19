package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class PrisonerDetailsTest {
  @Test
  fun `maps to device wearer`() {
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
      ),
    )
    val versionId = UUID.randomUUID()

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
  }
}
