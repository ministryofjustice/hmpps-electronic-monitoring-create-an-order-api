package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.core.io.InputStreamResource
import org.springframework.mock.env.MockEnvironment
import tools.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsAuthClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.Up3SimulationSettings
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.OrderChangeException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsDomainEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.Instant
import java.util.Optional
import java.util.UUID

class Up3SimulationServiceTest {
  private val simulations = mock<Up3SimulationRepository>()
  private val submissions = mock<FmsSubmissionResultRepository>()
  private val orders = mock<OrderRepository>()
  private val events = mock<Up3SimulationReturnEventRepository>()
  private val payloads = Up3SimulationPayloads(jacksonObjectMapper())
  private val receipts = mutableMapOf<UUID, Up3SimulationReturnEvent>()
  private val auth = mock<FmsAuthClient>()
  private val order = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
  private val simulation = Up3Simulation(order.id, order.versionId, "existing-case", enrolledBy = order.username)
  private lateinit var service: Up3SimulationService
  private lateinit var client: FmsClient

  @BeforeEach
  fun setup() {
    val env = MockEnvironment().withProperty("settings.return-listener-enabled", "true")
    env.setActiveProfiles("dev")
    service = Up3SimulationService(
      Up3SimulationSettings(true, env),
      simulations,
      submissions,
      orders,
      jacksonObjectMapper(),
      events,
      payloads,
    )
    client = FmsClient("http://localhost:1", auth, service)
    whenever(simulations.findById(order.id)).thenReturn(Optional.of(simulation))
    whenever(simulations.lockByOrderId(order.id)).thenReturn(simulation)
    whenever(simulations.findByCaseId("existing-case")).thenReturn(simulation)
    whenever(orders.findById(order.id)).thenReturn(Optional.of(order))
    whenever(simulations.save(any<Up3Simulation>())).thenAnswer { it.getArgument<Up3Simulation>(0) }
    whenever(events.lockById(any())).thenAnswer { receipts[it.getArgument<UUID>(0)] }
  }

  @Test
  fun `all submission endpoints produce consistent synthetic IDs without authentication`() {
    val created = client.createDeviceWearer("{}", order.id, FmsOrderSource.CEMO)
    val updated = client.updateDeviceWearer("{}", order.id, FmsOrderSource.CEMO)
    assertThat(created.result.single().id).isEqualTo("sim-${order.versionId}")
    assertThat(updated.result.single().id).isEqualTo(created.result.single().id)
    assertThat(client.createMonitoringOrder("{}", order.id, FmsOrderSource.CEMO).result.single().id)
      .isEqualTo("sim-mo-${order.versionId}")
    assertThat(client.updateMonitoringOrder("{}", order.id, FmsOrderSource.CEMO).result.single().id)
      .isEqualTo("sim-mo-${order.versionId}")
    verifyNoInteractions(auth)
  }

  @Test
  fun `old case IDs remain simulated after resubmission`() {
    val submission = FmsSubmissionResult(
      orderId = order.id,
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = "old-case"),
    )
    whenever(submissions.findByCaseId("old-case")).thenReturn(submission)
    order.getCurrentVersion().fmsResultId = submission.id
    order.status = OrderStatus.REJECTED
    assertThat(client.getState("old-case")).isEqualTo(CaseState.CANCELLED)
    verifyNoInteractions(auth)
  }

  @Test
  fun `feature off never routes enrolled orders back to real UP3`() {
    val env = MockEnvironment().also { it.setActiveProfiles("dev") }
    service = Up3SimulationService(
      Up3SimulationSettings(false, env),
      simulations,
      submissions,
      orders,
      jacksonObjectMapper(),
      events,
      payloads,
    )
    client = FmsClient("http://localhost:1", auth, service)
    assertThatThrownBy { client.createDeviceWearer("{}", order.id, FmsOrderSource.CEMO) }
      .isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy { client.updateDeviceWearer("{}", order.id, FmsOrderSource.CEMO) }
      .isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy { client.createMonitoringOrder("{}", order.id, FmsOrderSource.CEMO) }
      .isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy { client.updateMonitoringOrder("{}", order.id, FmsOrderSource.CEMO) }
      .isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy { client.getState("existing-case") }.isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy { client.getLatestOrderDetails("existing-case") }.isInstanceOf(OrderChangeException::class.java)
    assertThatThrownBy {
      client.createAttachment(
        "test.pdf",
        "existing-case",
        InputStreamResource("test".byteInputStream()),
        "LICENCE",
        RequestType.REQUEST,
      )
    }.isInstanceOf(OrderChangeException::class.java)
    verifyNoInteractions(auth)
  }

  @Test
  fun `normal operations outside dev never access optional persistence`() {
    val env = MockEnvironment().also { it.setActiveProfiles("prod") }
    val productionService = Up3SimulationService(
      Up3SimulationSettings(false, env),
      null,
      submissions,
      orders,
      jacksonObjectMapper(),
      null,
      payloads,
    )
    assertThat(productionService.find(order.id)).isNull()
    assertThat(productionService.deviceWearerResponse(order.id)).isNull()
    assertThat(productionService.monitoringOrderResponse(order.id)).isNull()
    assertThat(productionService.stateResponse("ordinary-case")).isNull()
    assertThat(productionService.detailsResponse("ordinary-case")).isNull()
    assertThat(productionService.attachmentResponse("ordinary-case", "test.pdf")).isNull()
    assertThat(productionService.captureNotifications(order, emptyList())).isFalse()
    var applied = false
    productionService.processReturn(
      ReturnsDomainEvent(ReturnMessage("ordinary-case", ReturnStatus.REJECTED, emptyList(), "2026-01-01T00:00:00Z")),
    ) { applied = true }
    assertThat(applied).isTrue()
    verifyNoInteractions(simulations, submissions, orders, events)
  }

  @Test
  fun `unknown synthetic IDs never escape to UP3`() {
    assertThatThrownBy { client.getState("sim-missing") }.isInstanceOf(OrderChangeException::class.java)
    verifyNoInteractions(auth)
  }

  @Test
  fun `unenrolled orders have no simulated response`() {
    whenever(simulations.lockByOrderId(order.id)).thenReturn(null)
    whenever(simulations.findById(order.id)).thenReturn(Optional.empty())
    assertThat(service.deviceWearerResponse(order.id)).isNull()
    assertThat(service.monitoringOrderResponse(order.id)).isNull()
    assertThat(service.stateResponse("another-case")).isNull()
  }

  @Test
  fun `real return events for enrolled cases are rejected without applying them`() {
    var rejected = false
    assertThatThrownBy {
      service.processReturn(
        event().copy(simulationOrderId = null, simulationVersionId = null, simulationEventId = null),
      ) { rejected = true }
    }.isInstanceOf(OrderChangeException::class.java)
    assertThat(rejected).isFalse()
  }

  @Test
  fun `incomplete simulation markers cannot be treated as a real event`() {
    assertThatThrownBy {
      service.processReturn(event().copy(simulationOrderId = null)) { error("must not reject") }
    }.isInstanceOf(OrderChangeException::class.java)
    verifyNoInteractions(orders, submissions)
  }

  @Test
  fun `retries do not repeat rejection or notifications`() {
    val event = event()
    var count = 0
    service.processReturn(event) { count++ }
    service.processReturn(event) { count++ }
    assertThat(count).isEqualTo(1)
    assertThat(simulation.lastProcessedEventId).isEqualTo(event.simulationEventId)
  }

  @Test
  fun `stale version events cannot reject a newer submission`() {
    assertThatThrownBy {
      service.processReturn(event().copy(simulationVersionId = UUID.randomUUID())) { error("must not reject") }
    }.isInstanceOf(OrderChangeException::class.java)
    verify(simulations, never()).save(any())
  }

  @Test
  fun `normal returns keep their existing processing`() {
    var rejected = false
    service.processReturn(
      ReturnsDomainEvent(ReturnMessage("another-case", ReturnStatus.REJECTED, emptyList(), "2026-01-01T00:00:00Z")),
    ) { rejected = true }
    assertThat(rejected).isTrue()
  }

  @Test
  fun `a valid delayed return is acknowledged without modifying a newer draft`() {
    val event = event()
    order.versions.add(
      OrderVersion(
        orderId = order.id,
        versionId = 1,
        username = order.username,
        status = OrderStatus.IN_PROGRESS,
        dataDictionaryVersion = order.dataDictionaryVersion,
        type = order.type,
      ),
    )
    assertThat(service.processReturn(event) { error("must not reject") }).isFalse()
    assertThat(receipts[event.simulationEventId]?.status).isEqualTo(SimulatedReturnStatus.STALE_IGNORED)
    assertThat(order.status).isEqualTo(OrderStatus.IN_PROGRESS)
    verify(simulations, never()).save(any())
  }

  @Test
  fun `a processing failure can be recorded separately and retried safely`() {
    val event = event()
    assertThatThrownBy { service.processReturn(event) { error("notification construction failed") } }
      .isInstanceOf(IllegalStateException::class.java)
    service.recordReturnFailure(event)
    val receipt = receipts[event.simulationEventId]!!
    assertThat(receipt.status).isEqualTo(SimulatedReturnStatus.FAILED)
    assertThat(receipt.failureStage).isEqualTo(SimulationFailureStage.PROCESSING)
    assertThat(receipt.processingFailures).isEqualTo(1)
    service.processReturn(event) {}
    assertThat(receipt.status).isEqualTo(SimulatedReturnStatus.PROCESSED)
  }

  @Test
  fun `unknown simulation receipts cannot apply a rejection`() {
    val event = event().copy(simulationEventId = UUID.randomUUID())
    assertThatThrownBy { service.processReturn(event) { error("must not reject") } }
      .isInstanceOf(OrderChangeException::class.java)
      .extracting("errorCode").isEqualTo("UP3_SIMULATION_INVALID_EVENT")
  }

  @Test
  fun `malformed deliveries cannot replace the failure state of a valid receipt`() {
    val event = event()
    service.recordReturnFailure(event.copy(data = event.data.copy(datetimeOfStatusChange = "not-a-date")))
    assertThat(receipts[event.simulationEventId]?.status).isEqualTo(SimulatedReturnStatus.PENDING_PUBLICATION)
    assertThat(receipts[event.simulationEventId]?.processingFailures).isZero()
  }

  private fun event(): ReturnsDomainEvent {
    val id = UUID.randomUUID()
    val timestamp = Instant.parse("2026-01-01T00:00:00Z")
    receipts[id] = Up3SimulationReturnEvent(
      id,
      order.id,
      order.versionId,
      order.versionId,
      "existing-case",
      payloads.returnHash(order.id, order.versionId, order.versionId, timestamp, emptyList()),
      "{}",
    )
    return ReturnsDomainEvent(
      ReturnMessage("existing-case", ReturnStatus.REJECTED, emptyList(), timestamp.toString()),
      simulationOrderId = order.id,
      simulationVersionId = order.versionId,
      simulationEventId = id,
    )
  }
}
