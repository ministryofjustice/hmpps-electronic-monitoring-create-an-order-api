package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderFieldCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderFieldChangeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderNegativeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo

class MonitoringOrderComapreTo {

  private fun baselineOrder() = MonitoringOrder(
    caseId = "CASE1",
    conditionType = "TypeA",
    orderStart = "2025-01-01",
    orderEnd = "2025-12-31",
    orderType = "Type1",
    notifyingOrganization = "Org",
    pduResponsible = "PDU",
    curfewStart = "19:00",
    curfewEnd = "06:00",
    abstinence = "No",
    enforceableCondition = mutableListOf(
      EnforceableCondition("Curfew", "2025-01-01"),
    ),
  )

  @ParameterizedTest(name = "changing {0} emits message")
  @ArgumentsSource(MonitoringOrderFieldChangeArgumentsProvider::class)
  fun `field change emits expected message`(case: MonitoringOrderFieldCase) {
    val old = baselineOrder()
    val updated = baselineOrder()

    case.mutate(updated)

    val result = updated.compareTo(old)

    assertThat(result).contains(case.expectedMessage)
  }

  @ParameterizedTest(name = "{0} should NOT emit message")
  @ArgumentsSource(MonitoringOrderNegativeArgumentsProvider::class)
  fun `unmapped fields do not trigger messages`(case: MonitoringOrderFieldCase) {
    val old = baselineOrder()
    val updated = baselineOrder()

    case.mutate(updated)

    val result = updated.compareTo(old)

    assertThat(result).isEmpty()
  }

  @Test
  fun `enforceable condition changes detected correctly`() {
    val old = baselineOrder().apply {
      enforceableCondition =
        mutableListOf(
          EnforceableCondition("Curfew", "2025-01-01"),
          EnforceableCondition("Exclusion", "2025-01-01"),
          EnforceableCondition("Trail", "2025-01-01", "2026-01-01"),
        )
    }

    val updated = baselineOrder().apply {
      enforceableCondition =
        mutableListOf(
          EnforceableCondition("Curfew", "2025-02-01"), // change start date
          EnforceableCondition("Trail", "2025-01-01", "2026-01-02"), // change end date
          EnforceableCondition("AML", "2025-01-01", "2026-01-02"), // Added
        )
    }

    val result = updated.compareTo(old)

    assertThat(result).containsAll(
      listOf(
        "Curfew start date has changed",
        "Exclusion has been deleted",
        "Trail end date has changed",
        "AML has been added",
      ),
    )
  }

  @Test
  fun `curfew duration changes detected correctly`() {
    val old = baselineOrder().apply {
      curfewDuration = mutableListOf(
        CurfewSchedule(
          location = "Primary",
          schedule = mutableListOf(
            Schedule(
              "Mo",
              start = "19:00",
              end = "07:00",
            ),
            Schedule(
              "Tu",
              start = "19:00",
              end = "07:00",
            ),
          ),
        ),
        CurfewSchedule(
          location = "Secondary",
          schedule = mutableListOf(
            Schedule(
              "Mo",
              start = "19:00",
              end = "07:00",
            ),
            Schedule(
              "Tu",
              start = "19:00",
              end = "07:00",
            ),
          ),
        ),
      )
    }

    val updated = baselineOrder().apply {
      curfewDuration = mutableListOf(
        CurfewSchedule(
          location = "Primary",
          schedule = mutableListOf(
            Schedule(
              "Mo",
              start = "19:00",
              end = "07:00",
            ),
            Schedule(
              "Tu",
              start = "19:00",
              end = "06:00",
            ),
          ),
        ),
        CurfewSchedule(
          location = "Tertiary",
          schedule = mutableListOf(
            Schedule(
              "Mo",
              start = "19:00",
              end = "07:00",
            ),
            Schedule(
              "Tu",
              start = "19:00",
              end = "07:00",
            ),
          ),
        ),
      )
    }

    val result = updated.compareTo(old)

    assertThat(result).containsAll(
      listOf(
        "Curfew timetable location primary has been changed",
        "Curfew timetable location secondary has been deleted",
        "Curfew timetable location tertiary has been added",
      ),
    )
  }
}
