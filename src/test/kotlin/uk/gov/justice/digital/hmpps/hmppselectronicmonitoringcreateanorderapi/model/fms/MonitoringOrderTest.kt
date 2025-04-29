package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Zone

@ActiveProfiles("test")
class MonitoringOrderTest : FmsTestBase() {

  @Test
  fun `It should map attendance monitoring to an FMS Monitoring Order`() {
    val order = createOrder(
      mandatoryAttendanceConditions = listOf(
        createMandatoryAttendanceCondition(),
      ),
    )
    val fmsMonitoringOrder = MonitoringOrder.fromOrder(order = order, caseId = "")

    assertThat(fmsMonitoringOrder.inclusionZones).isEqualTo(
      listOf(
        Zone(
          description = order.mandatoryAttendanceConditions[0].purpose + "\n" +
            order.mandatoryAttendanceConditions[0].appointmentDay + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine1 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine2 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine3 + "\n" +
            order.mandatoryAttendanceConditions[0].addressLine4 + "\n" +
            order.mandatoryAttendanceConditions[0].postcode + "\n",
          duration = "",
          start = "2025-01-01 12:00:00",
          end = "2025-02-01 13:00:00",
        ),
      ),
    )
    assertThat(fmsMonitoringOrder.enforceableCondition).contains(
      EnforceableCondition(
        condition = "Attendance Monitoring",
        startDate = "2025-01-01 01:01:01",
        endDate = "2025-02-01 01:01:01",
      ),
    )
  }
}
