package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.mock.env.MockEnvironment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.EmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsAuthClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.FmsClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.ManageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.Up3SimulationSettings
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.OrderChangeException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.Up3SimulationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsEventTranslator
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.Cohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.auth.UserCohort
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulateReturnDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedCaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnReasonDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateSimulatedCaseStateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.FmsService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.OrderService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderAndNotify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationControlService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationPayloads
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationReturnPublisher
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationSeedService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.UserCohortService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture

@DataJpaTest(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:simulation-db;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.flyway.enabled=false",
    "spring.test.database.replace=none",
  ],
)
@ActiveProfiles("dev")
class Up3SimulationWorkflowTest {
  @Autowired
  lateinit var orders: OrderRepository

  @Autowired
  lateinit var submissions: FmsSubmissionResultRepository

  @Autowired
  lateinit var simulations: Up3SimulationRepository

  @Autowired
  lateinit var details: FmsOrderDetailsRepository

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var returns: Up3SimulationReturnEventRepository

  @Autowired
  lateinit var creations: Up3SimulationCreationRepository

  @Autowired
  lateinit var transactions: PlatformTransactionManager

  private val mapper = JsonMapper.builder().addModule(KotlinModule.Builder().build())
    .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true).build()
  private val auth = mock<FmsAuthClient>()
  private val emails = mock<EmailClient>()
  private val sqs = mock<SqsAsyncClient>()
  private val token = mock<AuthAwareAuthenticationToken>()
  private lateinit var simulation: Up3SimulationService
  private lateinit var orderService: OrderService
  private lateinit var controls: Up3SimulationControlService
  private lateinit var translator: ReturnsEventTranslator
  private lateinit var publisher: Up3SimulationReturnPublisher

  @BeforeEach
  fun setup() {
    val env = MockEnvironment().withProperty("settings.return-listener-enabled", "true")
    env.setActiveProfiles("dev")
    val payloads = Up3SimulationPayloads(mapper)
    simulation = Up3SimulationService(
      Up3SimulationSettings(true, env),
      simulations,
      submissions,
      orders,
      mapper,
      returns,
      payloads,
    )
    val fmsClient = FmsClient("http://localhost:1", auth, simulation)
    val documentClient = mock<DocumentApiClient>()
    whenever(documentClient.getDocument(any())).thenAnswer {
      ResponseEntity.ok(Flux.just(InputStreamResource("test".byteInputStream())))
    }
    val flags = FeatureFlags(DataDictionaryVersion.DDV4, false, getApiEnabled = true)
    val events = mock<EventService>()
    val fmsService = FmsService(fmsClient, documentClient, mapper, submissions, true, flags, events, details)
    val userApi = mock<ManageUserApi>()
    val cohorts = mock<UserCohortService>()
    whenever(token.name).thenReturn("AUTH_ADM")
    whenever(token.token).thenReturn(mock<Jwt>())
    whenever(token.getUserFullName()).thenReturn("Test User")
    whenever(userApi.getUserEmail(any())).thenReturn("test@example.com")
    whenever(cohorts.getUserCohort(any())).thenReturn(UserCohort(Cohort.OTHER))
    whenever(cohorts.matchesNotifyingOrg(any(), any())).thenReturn(true)
    SecurityContextHolder.getContext().authentication = token
    orderService = OrderService(fmsService, flags, userApi).also {
      it.orderRepo = orders
      it.userCohortService = cohorts
    }
    val queues = mock<HmppsQueueService>()
    val queue = mock<HmppsQueue>()
    whenever(queues.findByQueueId("returnseventqueue")).thenReturn(queue)
    whenever(queue.queueUrl).thenReturn("http://localhost/returns.fifo")
    whenever(queue.sqsClient).thenReturn(sqs)
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("message-1").build()),
    )
    controls =
      Up3SimulationControlService(
        orderService, simulation, mapper, events, mock<Up3SimulationSeedService>(),
        orders, returns, creations, payloads, transactions,
      )
    publisher = Up3SimulationReturnPublisher(returns, queues, events)
    translator = ReturnsEventTranslator(
      RejectOrderAndNotify(
        RejectOrderService(JpaOrderByCaseIdGateway(submissions, orders), orders),
        orders,
        emails,
        simulation,
      ),
      mapper,
      events,
      simulation,
    )
  }

  @AfterEach
  fun cleanup() {
    SecurityContextHolder.clearContext()
  }

  @ParameterizedTest
  @EnumSource(value = RequestType::class, names = ["REQUEST", "VARIATION"])
  fun `existing order can lock return amend resubmit and return again with genuine history`(type: RequestType) {
    var order = existingSubmittedOrder(type)
    val originalVersionId = order.versionId
    assertThat(orderService.canAmend(order)).isFalse()
    assertThatThrownBy { orderService.amendOrder(order.id, token) }.isInstanceOf(OrderChangeException::class.java)

    val firstEvent = publishAndDeliver(order)
    translator.processEvent(firstEvent)
    order = orders.findById(order.id).orElseThrow()
    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
    assertThat(order.statusUpdates).hasSize(1)
    assertThat(order.statusUpdates.single().status).isEqualTo(ProcessingStatus.REJECTED)
    assertThat(order.statusUpdates.single().statusUpdateReasons.single().details).isEqualTo("Missing signature")
    assertThat(orderService.canAmend(order)).isTrue()
    assertThat(controls.get(order.id, token).capturedUserEmails).isEqualTo(1)
    assertThat(controls.get(order.id, token).capturedNoEmails).isEqualTo(1)

    order = orderService.amendOrder(order.id, token).order
    assertThat(order.versionId).isNotEqualTo(originalVersionId)
    assertThat(order.status).isEqualTo(OrderStatus.IN_PROGRESS)
    assertThat(order.statusUpdates).isEmpty()
    order.orderParameters?.haveLicence = true
    if (type == RequestType.VARIATION) {
      order.variationDetails = VariationDetails(
        versionId = order.versionId,
        variationDate = java.time.ZonedDateTime.now(),
        variationDetails = "Corrected returned details",
      )
    }
    order = orders.saveAndFlush(order)
    assertThat(order.isValid).isTrue()
    order = orderService.submitOrder(order.id, token, "Test User")
    assertThat(order.status).isEqualTo(OrderStatus.SUBMITTED)
    assertThat(orderService.canAmend(order)).isFalse()
    assertThat(submissions.findById(requireNotNull(order.fmsResultId)).orElseThrow().caseId)
      .isEqualTo("sim-${order.versionId}")
    assertThat(order.fmsResultDate).isNotNull()
    assertThat(order.submittedBy).isEqualTo("Test User")
    assertThat(orderService.getVersionInformation(order.id)).hasSize(2)
    val original = orderService.getSpecificVersion(order.id, originalVersionId)
    assertThat(original.statusUpdates).hasSize(1)

    publishAndDeliver(order)
    assertThat(orders.findById(order.id).orElseThrow().status).isEqualTo(OrderStatus.REJECTED)
    assertThat(controls.get(order.id, token).capturedUserEmails).isEqualTo(2)
    entityManager.flush()
    entityManager.clear()
    assertThat(simulations.findById(order.id).orElseThrow().capturedUserEmails).isEqualTo(2)
    assertThat(orders.findById(order.id).orElseThrow().status).isEqualTo(OrderStatus.REJECTED)
    verifyNoInteractions(auth, emails)
  }

  @Test
  fun `accepted case unlocks amendment using stored submission details`() {
    val order = existingSubmittedOrder()
    // Populate real mapped payloads through the normal submission strategy.
    order.status = OrderStatus.IN_PROGRESS
    orders.saveAndFlush(order)
    val submitted = orderService.submitOrder(order.id, token, "Test User")
    controls.setState(
      order.id,
      token,
      UpdateSimulatedCaseStateDto(SimulatedCaseState.ACCEPTED, order.versionId, order.versionId),
    )
    assertThat(orderService.canAmend(submitted)).isTrue()
    val acceptedCaseId = simulation.requireSimulation(order.id).caseId
    val amended = orderService.amendOrder(order.id, token)
    assertThat(amended.outcome).isEqualTo(ProcessingStatus.ACCEPTED)
    assertThat(amended.order.deviceWearer?.firstName).isEqualTo("John")
    simulation.deviceWearerResponse(order.id)
    assertThat(simulation.stateResponse(acceptedCaseId)).isEqualTo(CaseState.AWAITING_VALIDATION)
    verifyNoInteractions(auth)
  }

  @Test
  fun `non owners cannot control simulated orders`() {
    val order = existingSubmittedOrder()
    whenever(token.name).thenReturn("another-user")
    assertThatThrownBy {
      controls.setState(
        order.id,
        token,
        UpdateSimulatedCaseStateDto(SimulatedCaseState.ACCEPTED, order.versionId, order.versionId),
      )
    }
      .isInstanceOf(Up3SimulationException::class.java)
    assertThat(simulations.findById(order.id).orElseThrow().caseState).isEqualTo(CaseState.NEW)
  }

  @Test
  fun `queue publication failures have a durable failure outcome and recover without another intent`() {
    val order = existingSubmittedOrder()
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.failedFuture(IllegalStateException("Queue unavailable")),
    )
    val request = returnRequest(order)
    val receipt = controls.publishReturn(order.id, token, request)
    publisher.publish(receipt.eventId)
    val failed = returns.findById(receipt.eventId).orElseThrow()
    assertThat(failed.status).isEqualTo(SimulatedReturnStatus.FAILED)
    assertThat(failed.failureStage).isEqualTo(SimulationFailureStage.PUBLICATION)
    assertThat(failed.nextPublicationAttemptAt).isNotNull()
    assertThat(orders.findById(order.id).orElseThrow().status).isEqualTo(OrderStatus.SUBMITTED)
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("message-1").build()),
    )
    failed.nextPublicationAttemptAt = Instant.now().minusSeconds(1)
    publisher.publish(receipt.eventId)
    assertThat(failed.status).isEqualTo(SimulatedReturnStatus.QUEUED)
    assertThat(failed.publicationAttempts).isEqualTo(2)
    assertThat(returns.count()).isEqualTo(1)
  }

  private fun existingSubmittedOrder(type: RequestType = RequestType.REQUEST): Order {
    val order = TestUtilities.createReadyToSubmitOrder(
      status = OrderStatus.SUBMITTED,
      submittedBy = "Test User",
      requestType = type,
    )
    order.submittedByEmail = "test@example.com"
    order.interestedParties?.notifyingOrganisationEmail = "organisation@example.com"
    order.fmsResultDate = OffsetDateTime.now().minusHours(1)
    val result = submissions.saveAndFlush(
      FmsSubmissionResult(
        orderId = order.id,
        orderSource = FmsOrderSource.CEMO,
        strategy = FmsSubmissionStrategyKind.ORDER,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = UUID.randomUUID().toString()),
      ),
    )
    order.fmsResultId = result.id
    val stored = orders.saveAndFlush(order)
    simulations.saveAndFlush(
      Up3Simulation(stored.id, stored.versionId, requireNotNull(result.caseId), enrolledBy = stored.username),
    )
    return stored
  }

  private fun returnRequest(order: Order) = SimulateReturnDto(
    eventId = UUID.randomUUID(),
    datetimeOfStatusChange = Instant.now(),
    reasons = listOf(SimulatedReturnReasonDto("other", "Missing signature")),
    expectedCurrentOrderVersionId = order.versionId,
    expectedSimulatedSubmissionVersionId = order.versionId,
  )

  private fun publishAndDeliver(order: Order): String {
    val result = controls.publishReturn(order.id, token, returnRequest(order))
    publisher.publish(result.eventId)
    assertThat(returns.findById(result.eventId).orElseThrow().status).isEqualTo(SimulatedReturnStatus.QUEUED)
    val messages = argumentCaptor<SendMessageRequest>()
    verify(sqs, org.mockito.kotlin.atLeastOnce()).sendMessage(messages.capture())
    val message = messages.lastValue
    assertThat(message.messageGroupId()).startsWith("case:")
    val raw = message.messageBody()
    val timestamp = mapper.readTree(raw).path("data").path("datetimeOfStatusChange").asText()
    translator.processEvent(raw)
    val returned = orders.findById(order.id).orElseThrow()
    assertThat(returned.statusUpdates.single().datetimeOfStatusChange.toInstant()).isEqualTo(Instant.parse(timestamp))
    assertThat(controls.get(order.id, token).lastProcessedEventId).isEqualTo(result.eventId)
    return raw
  }
}
