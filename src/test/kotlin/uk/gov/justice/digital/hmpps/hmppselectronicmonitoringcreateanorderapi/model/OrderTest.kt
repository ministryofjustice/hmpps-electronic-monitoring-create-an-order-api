package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PrisonDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
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
  fun `It should return isValid false for order without responsible organisation`() {
    val order = createValidOrder()
    order.interestedParties!!.responsibleOrganisation = ""
    assertThat(order.isValid).isFalse()
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

  private fun createValidOrder(): Order = createOrder(
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
      notifyingOrganisationName = PrisonDDv5.GARTH_PRISON.name,
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
}
