package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.ZonedDateTime
import java.util.UUID

class OrderSearchRepositoryImplTest : IntegrationTestBase() {

  @BeforeEach
  fun cleanup() {
    repo.deleteAll()
  }

  @Test
  fun `search uses the monitoring conditions dates when they are present`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.SUBMITTED,
      startDate = ZonedDateTime.parse("2040-02-07T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-07T00:00:00Z"),
    )

    // Child dates outside the MonitoringConditions range
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2040-01-01T00:00:00Z")
    order.monitoringConditionsAlcohol!!.endDate = ZonedDateTime.parse("2040-06-01T00:00:00Z")
    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    // Monitoring conditions dates take precedence if they exist
    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-02-07T00:00:00Z").toInstant())
    assertThat(result.monitoringConditions.endDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-03-07T00:00:00Z").toInstant())
  }

  @Test
  fun `search infers the order window from child conditions when monitoring conditions dates are absent`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.SUBMITTED,
      startDate = ZonedDateTime.parse("2040-02-07T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-07T00:00:00Z"),
    )
    // Extreme date
    val extremeDate = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.SUBMITTED,
      startDate = ZonedDateTime.parse("2000-02-07T00:00:00Z"),
      endDate = ZonedDateTime.parse("2099-03-07T00:00:00Z"),
    )

    extremeDate.monitoringConditions!!.startDate = null
    extremeDate.monitoringConditions!!.endDate = null
    repo.save(extremeDate)

    order.monitoringConditions!!.startDate = null
    order.monitoringConditions!!.endDate = null

    // Result start
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2040-01-05T00:00:00Z")
    order.monitoringConditionsTrail!!.endDate = ZonedDateTime.parse("2040-02-20T00:00:00Z")
    order.monitoringConditionsAlcohol!!.startDate = ZonedDateTime.parse("2040-01-20T00:00:00Z")
    // Result end
    order.monitoringConditionsAlcohol!!.endDate = ZonedDateTime.parse("2040-04-15T00:00:00Z")
    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-01-05T00:00:00Z").toInstant())
    assertThat(result.monitoringConditions.endDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-04-15T00:00:00Z").toInstant())
  }

  @Test
  fun `search returns the stored monitoring dates`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.IN_PROGRESS,
      startDate = ZonedDateTime.parse("2040-02-01T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-01T00:00:00Z"),
    )
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2040-01-05T00:00:00Z")
    order.monitoringConditionsAlcohol!!.endDate = ZonedDateTime.parse("2040-04-15T00:00:00Z")
    order.recalculateMonitoringStartEndDate()

    val decoy = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.IN_PROGRESS,
      startDate = ZonedDateTime.parse("2000-01-01T00:00:00Z"),
      endDate = ZonedDateTime.parse("2099-12-31T00:00:00Z"),
    )
    repo.saveAll(listOf(order, decoy))

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-01-05T00:00:00Z").toInstant())
    assertThat(result.monitoringConditions.endDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-04-15T00:00:00Z").toInstant())
  }

  @Test
  fun `search falls back to the monitoring conditions dates for legacy orders`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.IN_PROGRESS,
      startDate = ZonedDateTime.parse("2040-02-01T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-01T00:00:00Z"),
    )
    order.getCurrentVersion().monitoringStartDate = null
    order.getCurrentVersion().monitoringEndDate = null
    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-02-01T00:00:00Z").toInstant())
  }

  @Test
  fun `search derives from the monitoring types when nothing is stored`() {
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.IN_PROGRESS,
      startDate = ZonedDateTime.parse("2040-02-01T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-01T00:00:00Z"),
    )
    order.getCurrentVersion().monitoringStartDate = null
    order.getCurrentVersion().monitoringEndDate = null
    order.monitoringConditions!!.startDate = null
    order.monitoringConditions!!.endDate = null
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2040-01-05T00:00:00Z")
    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(ZonedDateTime.parse("2040-01-05T00:00:00Z").toInstant())
  }

  @Test
  fun `recalculateMonitoringStartEndDate is inert when order status is not in progress`() {
    val staticResultStartDate = ZonedDateTime.parse("2040-02-01T00:00:00Z")
    val staticResultEndDate = ZonedDateTime.parse("2040-03-01T00:00:00Z")
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.SUBMITTED,
      startDate = staticResultStartDate,
      endDate = staticResultEndDate,
    )
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2039-01-05T00:00:00Z")
    order.monitoringConditionsAlcohol!!.endDate = ZonedDateTime.parse("2050-04-15T00:00:00Z")

    order.recalculateMonitoringStartEndDate()

    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(staticResultStartDate.toInstant())
    assertThat(result.monitoringConditions.endDate!!.toInstant())
      .isEqualTo(staticResultEndDate.toInstant())
  }

  @Test
  fun `recalculateMonitoringStartEndDate updates when order status is in progress`() {
    val overriderStartDate = ZonedDateTime.parse("2039-01-05T00:00:00Z")
    val overriderEndDate = ZonedDateTime.parse("2050-04-15T00:00:00Z")
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = UUID.randomUUID(),
      status = OrderStatus.IN_PROGRESS,
      startDate = ZonedDateTime.parse("2040-02-01T00:00:00Z"),
      endDate = ZonedDateTime.parse("2040-03-01T00:00:00Z"),
    )
    order.monitoringConditionsTrail!!.startDate = ZonedDateTime.parse("2039-01-05T00:00:00Z")
    order.monitoringConditionsAlcohol!!.endDate = ZonedDateTime.parse("2050-04-15T00:00:00Z")

    order.recalculateMonitoringStartEndDate()

    repo.save(order)

    val result = repo.searchOrders(OrderSearchCriteria()).single { it.id == order.id }

    assertThat(result.monitoringConditions.startDate!!.toInstant())
      .isEqualTo(overriderStartDate.toInstant())
    assertThat(result.monitoringConditions.endDate!!.toInstant())
      .isEqualTo(overriderEndDate.toInstant())
  }
}
