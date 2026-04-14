package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@ActiveProfiles("test")
class OrderTest : OrderTestBase() {

  @Test
  fun `It should return isValid true for valid order`() {
    val order = createValidOrder()
    assertThat(order.isValid).isTrue()
  }

  @Test
  fun `It should return isValid false for order without notifying organisation`() {
    val order = createValidOrder()
    order.interestedParties!!.notifyingOrganisation = ""
    assertThat(order.isValid).isFalse()
  }

  @Test
  fun `It should return isValid false for order without licence`() {
    val order = createValidOrder()
    order.additionalDocuments.clear()
    assertThat(order.isValid).isFalse()
  }

  @Test
  fun `It should return isValid for home office order without licence`() {
    val order = createValidOrder()
    order.additionalDocuments.clear()
    order.interestedParties = createInterestedParty(notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE.name)
    assertThat(order.isValid).isTrue()
  }

  @Test
  fun `It should return isValid true for order without responsible organisation`() {
    val order = createValidOrder()
    order.interestedParties!!.responsibleOrganisation = ""
    assertThat(order.isValid).isTrue()
  }

  @Test
  fun `It should return isValid false for order without any monitoring condition`() {
    val order = createValidOrder()
    order.curfewConditions = null
    order.curfewReleaseDateConditions = null
    order.curfewTimeTable.clear()
    order.monitoringConditionsAlcohol = null
    order.monitoringConditionsTrail = null
    order.enforcementZoneConditions.clear()
    order.mandatoryAttendanceConditions.clear()
    assertThat(order.isValid).isFalse()
  }

  @Test
  fun `It should return isValid false for order without curfew release date`() {
    val order = createValidOrder()
    order.curfewConditions = createCurfewConditions(
      startDate = ZonedDateTime.now(),
    )
    order.curfewReleaseDateConditions = null

    order.monitoringConditionsAlcohol = null
    order.monitoringConditionsTrail = null
    order.enforcementZoneConditions.clear()
    order.mandatoryAttendanceConditions.clear()
    assertThat(order.isValid).isFalse()
  }

  @Test
  fun `It should return isValid false for order without curfew timetable`() {
    val order = createValidOrder()
    order.curfewConditions = createCurfewConditions(
      startDate = ZonedDateTime.now(),
    )
    order.curfewReleaseDateConditions = createCurfewDayOfReslse()

    order.monitoringConditionsAlcohol = null
    order.monitoringConditionsTrail = null
    order.enforcementZoneConditions.clear()
    order.mandatoryAttendanceConditions.clear()
    assertThat(order.isValid).isFalse()
  }

  @Test
  fun `It should return isValid true for order with curfew`() {
    val order = createValidOrder()
    order.curfewConditions = createCurfewConditions(
      startDate = ZonedDateTime.now(),
    )
    order.curfewReleaseDateConditions = createCurfewDayOfReslse()
    order.curfewTimeTable = createCurfewTimeTable()
    order.monitoringConditionsAlcohol = null
    order.monitoringConditionsTrail = null
    order.enforcementZoneConditions.clear()
    order.mandatoryAttendanceConditions.clear()
    assertThat(order.isValid).isTrue()
  }

  @Test
  fun `getMonitoringStartDate should return monitoringConditions startDate when set`() {
    val expectedStartDate = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        startDate = expectedStartDate,
        trail = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        startDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
    )

    val result = order.getMonitoringStartDate()

    assertThat(result).isEqualTo(expectedStartDate)
  }

  @Test
  fun `getMonitoringStartDate should return earliest date from all conditions when null`() {
    val earliestDate = ZonedDateTime.of(2025, 1, 10, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        startDate = null,
        trail = true,
        curfew = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        startDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      curfewConditions = CurfewConditions(
        versionId = UUID.randomUUID(),
        startDate = earliestDate,
      ),
    )

    val result = order.getMonitoringStartDate()

    assertThat(result).isEqualTo(earliestDate)
  }

  @Test
  fun `getMonitoringStartDate should handle all conditions`() {
    val earliestDate = ZonedDateTime.of(2025, 1, 10, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        startDate = null,
        trail = true,
        curfew = true,
        alcohol = true,
        mandatoryAttendance = true,
        exclusionZone = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        startDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      curfewConditions = CurfewConditions(
        versionId = UUID.randomUUID(),
        startDate = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      alcoholMonitoringConditions = createAlcoholMonitoringConditions(
        startDate = earliestDate,
      ),
      mandatoryAttendanceConditions = listOf(
        createMandatoryAttendanceCondition(
          startDate = ZonedDateTime.of(2025, 1, 25, 10, 0, 0, 0, ZoneId.of("UTC")),
        ),
      ),
      enforcementZoneConditions = listOf(
        createEnforcementZoneCondition(
          startDate = ZonedDateTime.of(2025, 1, 30, 10, 0, 0, 0, ZoneId.of("UTC")),
        ),
      ),
    )

    val result = order.getMonitoringStartDate()

    assertThat(result).isEqualTo(earliestDate)
  }

  @Test
  fun `getMonitoringEndDate should return monitoringConditions endDate when set`() {
    val expectedEndDate = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        endDate = expectedEndDate,
        trail = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        endDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
    )

    val result = order.getMonitoringEndDate()

    assertThat(result).isEqualTo(expectedEndDate)
  }

  @Test
  fun `getMonitoringEndDate should return latest date from all conditions when null`() {
    val latestDate = ZonedDateTime.of(2025, 1, 30, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        endDate = null,
        trail = true,
        curfew = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        endDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      curfewConditions = CurfewConditions(
        versionId = UUID.randomUUID(),
        endDate = latestDate,
      ),
    )

    val result = order.getMonitoringEndDate()

    assertThat(result).isEqualTo(latestDate)
  }

  @Test
  fun `getMonitoringEndDate should handle all conditions`() {
    val latestDate = ZonedDateTime.of(2025, 1, 30, 10, 0, 0, 0, ZoneId.of("UTC"))

    val order = createOrder(
      monitoringConditions = createMonitoringConditions(
        endDate = null,
        trail = true,
        curfew = true,
        alcohol = true,
        mandatoryAttendance = true,
        exclusionZone = true,
      ),
      trailMonitoringConditions = TrailMonitoringConditions(
        versionId = UUID.randomUUID(),
        endDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      curfewConditions = CurfewConditions(
        versionId = UUID.randomUUID(),
        endDate = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC")),
      ),
      alcoholMonitoringConditions = createAlcoholMonitoringConditions(
        endDate = latestDate,
      ),
      mandatoryAttendanceConditions = listOf(
        createMandatoryAttendanceCondition(
          endDate = ZonedDateTime.of(2025, 1, 25, 10, 0, 0, 0, ZoneId.of("UTC")),
        ),
      ),
      enforcementZoneConditions = listOf(
        createEnforcementZoneCondition(
          endDate = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC")),
        ),
      ),
    )

    val result = order.getMonitoringEndDate()

    assertThat(result).isEqualTo(latestDate)
  }

  @ParameterizedTest
  @MethodSource("variationRequestTypeProvider")
  fun `should return false if request type is a variation type but variation details is not set`(
    requestType: RequestType,
  ) {
    val order = createValidOrder(requestType)
    order.variationDetails = null
    assertThat(order.isValid).isFalse()
  }

  @ParameterizedTest
  @MethodSource("variationRequestTypeProvider")
  fun `should return true if request type is a variation type and variation details is  set`(requestType: RequestType) {
    val order = createValidOrder(requestType)
    order.variationDetails = VariationDetails(
      versionId = UUID.randomUUID(),
      variationType = VariationType.CHANGE_TO_ADDRESS,
      variationDate = ZonedDateTime.now(),
      variationDetails = "Mock variation",
    )

    assertThat(order.isValid).isTrue()
  }

  private fun createValidOrder(requestType: RequestType = RequestType.REQUEST): Order = createOrder(
    type = requestType,
    monitoringConditions = createMonitoringConditions(
      trail = true,
    ),
    trailMonitoringConditions = TrailMonitoringConditions(
      versionId = UUID.randomUUID(),
      startDate = ZonedDateTime.now(),
    ),
    installationLocation = InstallationLocation(
      versionId = UUID.randomUUID(),
      location = InstallationLocationType.PRIMARY,
    ),
    interestedParties = createInterestedParty(
      responsibleOrganisation = ResponsibleOrganisation.PROBATION.name,
      responsibleOfficerName = "John Smith",
      responsibleOfficerPhoneNumber = "07408888888",
      responsibleOrganisationRegion = ProbationServiceRegion.EAST_MIDLANDS.name,
      responsibleOrganisationEmail = "",
      notifyingOrganisation = NotifyingOrganisationDDv5.PRISON.name,
      notifyingOrganisationName = Prison.GARTH_PRISON.name,
      notifyingOrganisationEmail = "",
    ),
    additionalDocuments = mutableListOf(
      AdditionalDocument(
        versionId = UUID.randomUUID(),
        fileType = DocumentType.LICENCE,
        fileName = "test file",
        documentId = UUID.randomUUID(),
      ),
    ),
  )

  companion object {
    @JvmStatic
    fun variationRequestTypeProvider(): List<Arguments> = listOf(

      Arguments.of(RequestType.VARIATION),
      Arguments.of(RequestType.REINSTALL_AT_DIFFERENT_ADDRESS),
      Arguments.of(RequestType.REINSTALL_DEVICE),
      Arguments.of(RequestType.REVOCATION),
      Arguments.of(RequestType.END_MONITORING),
    )
  }
}
