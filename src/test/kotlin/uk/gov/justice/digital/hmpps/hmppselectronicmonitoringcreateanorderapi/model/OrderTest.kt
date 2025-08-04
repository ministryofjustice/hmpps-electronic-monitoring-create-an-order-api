package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PrisonDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
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
  fun `It should return isValid false for order without responsible organisation`() {
    val order = createValidOrder()
    order.interestedParties!!.responsibleOrganisation = ""
    assertThat(order.isValid).isFalse()
  }

  private fun createValidOrder(): Order = createOrder(
    monitoringConditions = createMonitoringConditions(
      trail = true,
    ),
    trailMonitoringConditions = TrailMonitoringConditions(versionId = UUID.randomUUID()),
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
  )
}
