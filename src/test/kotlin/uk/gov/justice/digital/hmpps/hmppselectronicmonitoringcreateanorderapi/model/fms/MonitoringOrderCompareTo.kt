package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderFieldCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderFieldChangeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderNegativeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderOVTCase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider.MonitoringOrderOVTTypeArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.CurfewSchedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.EnforceableCondition
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.Schedule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.compareTo

class MonitoringOrderCompareTo {

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

    assertThat(result.messages).contains(case.expectedMessage)
  }

  @ParameterizedTest(name = "{0} should NOT emit message")
  @ArgumentsSource(MonitoringOrderNegativeArgumentsProvider::class)
  fun `unmapped fields do not trigger messages`(case: MonitoringOrderFieldCase) {
    val old = baselineOrder()
    val updated = baselineOrder()

    case.mutate(updated)

    val result = updated.compareTo(old)

    assertThat(result.messages).isEmpty()
  }

  @Test
  fun `Should not send message if responsible organisation related fields been removed`() {
    val old = baselineOrder().apply {
      responsibleOrganization = "Mock RO"
      roEmail = "mockROEmail"
      roRegion = "mockRORegion"
      responsibleOfficerEmail = "mockOfficerEmail"
      responsibleOfficerName = "mockOfficerName"
      pduResponsible = "Mock pdu responsible"
    }
    val new = baselineOrder().apply {
      responsibleOrganization = ""
      roEmail = ""
      roRegion = ""
      responsibleOfficerEmail = ""
      responsibleOfficerName = ""
      pduResponsible = ""
    }

    val result = new.compareTo(old)

    assertThat(result.messages).doesNotContainAnyElementsOf(
      listOf(
        "PDU has changed",
        "Responsible officer's email has changed",
        "Responsible officer's name has changed",
        "Responsible organisation has changed",
        "Responsible organisation email has changed",
        "Responsible organisation region has changed",
      ),
    )
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

    assertThat(result.messages).containsAll(
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

    assertThat(result.messages).containsAll(
      listOf(
        "Curfew timetable for primary address has been changed",
        "Curfew timetable for secondary address has been deleted",
        "Curfew timetable for tertiary address has been added",
      ),
    )
  }

  @ParameterizedTest(name = "changing {0} produces expected variation type")
  @ArgumentsSource(MonitoringOrderOVTTypeArgumentsProvider::class)
  fun `changing order variation fields produces expected variation`(case: MonitoringOrderOVTCase) {
    val old = baselineOrder()
    val updated = baselineOrder()

    case.mutate(updated)

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType).isEqualTo(case.expected)
  }

  @Test
  fun `setting same value should not produce variation`() {
    val old = baselineOrder()
    val updated = baselineOrder()

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType).isEqualTo(VariationType.OTHER)
  }

  @Test
  fun `null to null should not produce variation`() {
    val old = baselineOrder()
    val updated = baselineOrder()

    old.curfewEnd = null
    updated.curfewEnd = null

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType).isEqualTo(VariationType.OTHER)
  }

  @Test
  fun `multiple non ovt fields should not produce variation`() {
    val old = baselineOrder()
    val updated = baselineOrder()

    updated.pilot = "new pilot"
    updated.releasedUnderPrarr = "new prarr"

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType).isEqualTo(VariationType.OTHER)
  }

  @Test
  fun `multiple of the same variation type change should produce single variation type`() {
    val old = baselineOrder()
    val updated = baselineOrder()

    updated.curfewDescription = "new description"
    updated.abstinence = "new abstinence"

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType)
      .isEqualTo(VariationType.CHANGE_TO_ENFORCEABLE_CONDITION)
  }

  @Test
  fun `only the most important change is returned`() {
    val old = baselineOrder()
    val updated = baselineOrder()

    updated.curfewDescription = "new description"
    updated.crownCourtCaseReferenceNumber = "new case number"

    val result = updated.compareTo(old)

    Assertions.assertThat(result.orderVariationType)
      .isEqualTo(
        VariationType.CHANGE_TO_ENFORCEABLE_CONDITION,
      )
  }
}
