package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities.SqsTestQueueFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities.TestEmailClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.ManageUserApiExtension.Companion.manageUserApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsEventTranslator
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Dapo
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DetailsOfInstallation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OffenceAdditionalDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationCreation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreatedSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulateReturnDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedCaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedOrderState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnEventDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnReasonDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.Up3SimulationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateSimulatedCaseStateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsCaseload
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserCaseloadResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.hmpps.HmppsUserEmailResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationCreationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderAndNotify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationControlService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationPayloads
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationReturnPublisher
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ActiveProfiles("dev")
@TestPropertySource(
  properties = [
    "spring.datasource.url=jdbc:h2:mem:simulation-enabled;MODE=PostgreSQL",
    "settings.up3-simulation-enabled=true",
    "hmpps.sqs.queues.returnseventqueue.queueName=simulation_returns_events_queue.fifo",
    "hmpps.sqs.queues.returnseventqueue.queueArn=arn:aws:sqs:eu-west-2:000000000000:simulation_returns_events_queue.fifo",
    "hmpps.sqs.queues.returnseventqueue.dlqName=simulation_returns_events_dead_letter_queue.fifo",
  ],
)
class Up3SimulationControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var submissions: FmsSubmissionResultRepository

  @Autowired
  lateinit var queues: SqsTestQueueFactory

  @Autowired
  lateinit var emails: TestEmailClient

  @Autowired
  lateinit var mapper: ObjectMapper

  @Autowired
  lateinit var simulations: Up3SimulationRepository

  @Autowired
  lateinit var returns: Up3SimulationReturnEventRepository

  @Autowired
  lateinit var creations: Up3SimulationCreationRepository

  @Autowired
  lateinit var payloads: Up3SimulationPayloads

  @Autowired
  lateinit var transactionManager: PlatformTransactionManager

  @Autowired
  lateinit var controls: Up3SimulationControlService

  @Autowired
  lateinit var translator: ReturnsEventTranslator

  @MockitoSpyBean
  lateinit var publisher: Up3SimulationReturnPublisher

  @MockitoSpyBean
  lateinit var rejection: RejectOrderAndNotify

  private val roles = listOf("ROLE_EM_CEMO__CREATE_ORDER")
  private val queue by lazy { queues.create("returnseventqueue") }

  @BeforeEach
  fun setup() {
    queue.purge()
    queue.purgeDlq()
    emails.reset()
  }

  @Test
  fun `returns a simulated order through SQS and exposes real timeline status data`() {
    val order = createSubmittedOrder(submittedByEmail = "test@example.com")
    val result = submissions.save(
      FmsSubmissionResult(
        orderId = order.id,
        orderSource = FmsOrderSource.CEMO,
        strategy = FmsSubmissionStrategyKind.ORDER,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = UUID.randomUUID().toString()),
      ),
    )
    order.fmsResultId = result.id
    repo.save(order)
    val path = "/api/dev/up3-simulation/orders/${order.id}"
    simulations.save(Up3Simulation(order.id, order.versionId, requireNotNull(result.caseId), enrolledBy = testUser))
    webTestClient.get().uri(path).headers(simulationAuthorisation())
      .exchange().expectStatus().isOk
      .expectBody().jsonPath("$.caseState").isEqualTo("NEW")
    assertThat(getOrder(order.id).canAmend).isFalse()

    val eventId = UUID.randomUUID()
    val timestamp = Instant.now().minusSeconds(1).truncatedTo(ChronoUnit.MICROS)
    val request = """
      {
        "eventId": "$eventId",
        "expectedCurrentOrderVersionId": "${order.versionId}",
        "expectedSimulatedSubmissionVersionId": "${order.versionId}",
        "datetimeOfStatusChange": "$timestamp",
        "reasons": [{"section": "other", "details": "Missing signature"}]
      }
    """.trimIndent()
    webTestClient.post().uri("$path/return").headers(simulationAuthorisation())
      .contentType(MediaType.APPLICATION_JSON).bodyValue(request)
      .exchange().expectStatus().isAccepted
      .expectBody().jsonPath("$.eventId").isEqualTo(eventId.toString())

    await().untilAsserted {
      val returned = getOrder(order.id)
      assertThat(returned.status).isEqualTo(OrderStatus.REJECTED)
      assertThat(returned.canAmend).isTrue()
      assertThat(returned.statusUpdates).hasSize(1)
      assertThat(returned.statusUpdates.single().datetimeOfStatusChange.toInstant()).isEqualTo(timestamp)
      assertThat(returned.statusUpdates.single().statusUpdateReasons.single().details).isEqualTo("Missing signature")
    }
    val state = webTestClient.get().uri(path).headers(simulationAuthorisation())
      .exchange().expectStatus().isOk.expectBody(Up3SimulationDto::class.java).returnResult().responseBody!!
    assertThat(state.lastProcessedEventId).isEqualTo(eventId)
    assertThat(state.capturedUserEmails).isEqualTo(1)
    assertThat(queue.dlqIsEmpty()).isTrue()
    emails.assertNothingSent()
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @Test
  fun `requires authentication and the normal role without another credential`() {
    val path = "/api/dev/up3-simulation/orders/${UUID.randomUUID()}"
    webTestClient.get().uri(path).exchange().expectStatus().isForbidden
    webTestClient.get().uri(path).headers(simulationAuthorisation(emptyList()))
      .exchange().expectStatus().isForbidden
    webTestClient.get().uri(path).headers(setAuthorisation()).exchange().expectStatus().isNotFound
  }

  @ParameterizedTest
  @EnumSource(value = OrderStatus::class, names = ["IN_PROGRESS", "SUBMITTED", "REJECTED"])
  fun `ordinary orders cannot be enrolled or manipulated`(status: OrderStatus) {
    val order = createStoredOrder().also {
      it.status = status
      repo.save(it)
    }
    val updated = order.lastUpdatedDateTime
    val path = simulationPath(order.id)
    webTestClient.post().uri(path).headers(simulationAuthorisation())
      .bodyValue(mapOf("expectedCurrentOrderVersionId" to order.versionId))
      .exchange().expectStatus().isEqualTo(405)
    webTestClient.put().uri("$path/state").headers(simulationAuthorisation())
      .bodyValue(UpdateSimulatedCaseStateDto(SimulatedCaseState.ACCEPTED, order.versionId, order.versionId))
      .exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_NOT_ENROLLED")
    webTestClient.post().uri("$path/return").headers(simulationAuthorisation())
      .bodyValue(
        SimulateReturnDto(
          UUID.randomUUID(),
          Instant.now(),
          listOf(SimulatedReturnReasonDto("other", "Test")),
          order.versionId,
          order.versionId,
        ),
      ).exchange().expectStatus().isNotFound
    assertThat(simulations.findById(order.id)).isEmpty()
    val unchanged = repo.findById(order.id).orElseThrow()
    assertThat(unchanged.status).isEqualTo(status)
    assertThat(unchanged.versionId).isEqualTo(order.versionId)
    assertThat(unchanged.lastUpdatedDateTime).isEqualTo(updated)
    assertThat(unchanged.statusUpdates).isEmpty()
  }

  @Test
  fun `validates return payloads and state values before processing`() {
    val path = "/api/dev/up3-simulation/orders/${UUID.randomUUID()}"
    webTestClient.post().uri("$path/return").headers(simulationAuthorisation())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue("""{"eventId":"${UUID.randomUUID()}","datetimeOfStatusChange":"${Instant.now()}","reasons":[]}""")
      .exchange().expectStatus().isBadRequest
    webTestClient.put().uri("$path/state").headers(simulationAuthorisation())
      .contentType(MediaType.APPLICATION_JSON).bodyValue("""{"state":"CANCELLED"}""")
      .exchange().expectStatus().isBadRequest
  }

  @ParameterizedTest
  @EnumSource(value = RequestType::class, names = ["REQUEST", "VARIATION"])
  fun `existing populated order can return amend resubmit and return again through HTTP and SQS`(type: RequestType) {
    val userRoles = roles + "ROLE_PRISON"
    manageUserApi.stubUserActiveCaseLoad(
      HmppsUserCaseloadResponse(active = true, activeCaseload = HmppsCaseload("CADM_I", "ADMIN")),
    )
    manageUserApi.stubGetUserEmail(HmppsUserEmailResponse(testUser, "test@example.com", true))
    var order = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED, requestType = type)
    order.submittedByEmail = "test@example.com"
    val originalVersionId = order.versionId
    order.fmsResultId = submissions.save(
      FmsSubmissionResult(
        orderId = order.id,
        orderSource = FmsOrderSource.CEMO,
        strategy = FmsSubmissionStrategyKind.ORDER,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = UUID.randomUUID().toString()),
      ),
    ).id
    order = repo.save(order)
    order.additionalDocuments.forEach { documentApi.stubGetDocument(it.documentId.toString()) }
    order.enforcementZoneConditions.filter { it.fileId != null }.forEach {
      documentApi.stubGetDocument(it.fileId.toString())
    }
    val path = "/api/dev/up3-simulation/orders/${order.id}"
    val caseId = requireNotNull(submissions.findById(requireNotNull(order.fmsResultId)).orElseThrow().caseId)
    simulations.save(Up3Simulation(order.id, order.versionId, caseId, enrolledBy = testUser))
    webTestClient.get().uri(path).headers(simulationAuthorisation(userRoles)).exchange().expectStatus().isOk
    assertThat(getOrder(order.id).canAmend).isFalse()

    fun returnCurrentSubmission(): UUID {
      val eventId = UUID.randomUUID()
      webTestClient.post().uri("$path/return").headers(simulationAuthorisation(userRoles))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          """
          {
            "eventId":"$eventId",
            "expectedCurrentOrderVersionId":"${getOrder(order.id).versionId}",
            "expectedSimulatedSubmissionVersionId":"${getOrder(order.id).versionId}",
            "datetimeOfStatusChange":"${Instant.now()}",
            "reasons":[{"section":"other","details":"Missing signature"}]
          }
          """.trimIndent(),
        ).exchange().expectStatus().isAccepted
      await().untilAsserted {
        assertThat(getOrder(order.id).status).isEqualTo(OrderStatus.REJECTED)
      }
      return eventId
    }

    val firstEvent = returnCurrentSubmission()
    webTestClient.post().uri("/api/orders/${order.id}/amend")
      .headers(setAuthorisation(roles = userRoles)).exchange().expectStatus().isOk
    order = repo.findById(order.id).orElseThrow()
    order.orderParameters?.haveLicence = true
    if (type == RequestType.VARIATION) {
      order.variationDetails = VariationDetails(
        versionId = order.versionId,
        variationDate = java.time.ZonedDateTime.now(),
        variationDetails = "Corrected returned details",
      )
    }
    repo.save(order)
    val submitted = webTestClient.post().uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation(roles = userRoles)).exchange().expectStatus().isOk
      .expectBody(OrderDto::class.java).returnResult().responseBody!!
    assertThat(submitted.status).isEqualTo(OrderStatus.SUBMITTED)
    assertThat(submitted.canAmend).isFalse()
    assertThat(submitted.fmsResultDate).isNotNull()
    assertThat(submitted.versionId).isNotEqualTo(originalVersionId)
    webTestClient.get().uri("/api/orders/${order.id}/versions")
      .headers(setAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.length()").isEqualTo(2)
    webTestClient.get().uri("/api/orders/${order.id}/versions/$originalVersionId")
      .headers(setAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.statusUpdates.length()").isEqualTo(1)
    val secondEvent = returnCurrentSubmission()
    listOf(firstEvent, secondEvent).forEach {
      assertThat(getReturnEvent(order.id, it).status).isEqualTo(SimulatedReturnStatus.PROCESSED)
    }
    emails.assertNothingSent()
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @ParameterizedTest
  @CsvSource(
    "DRAFT,REQUEST",
    "PROCESSING,REQUEST",
    "ACCEPTED,REQUEST",
    "RETURNED,REQUEST",
    "DRAFT,VARIATION",
    "PROCESSING,VARIATION",
    "ACCEPTED,VARIATION",
    "RETURNED,VARIATION",
  )
  fun `creates a fresh populated simulated order in the requested state`(
    state: SimulatedOrderState,
    type: RequestType,
  ) {
    val source = seedSource(type)
    val reasons = if (state == SimulatedOrderState.RETURNED) {
      listOf(SimulatedReturnReasonDto("other", "Correct this test order"))
    } else {
      emptyList()
    }
    val created = createSimulated(CreateSimulatedOrderDto(source, state, reasons, UUID.randomUUID()))
    val id = created.simulation.orderId
    val snapshot = getOrder(id)
    assertThat(id).isNotEqualTo(source.id)
    assertThat(snapshot.versionId).isNotEqualTo(source.versionId)
    assertThat(snapshot.username).isEqualTo(testUser)
    assertThat(snapshot.lastUpdatedBy).isEqualTo("John Smith")
    assertThat(snapshot.lastUpdatedDateTime).isAfter(source.lastUpdatedDateTime)
    assertThat(snapshot.ownerCohort).isEqualTo("WAYLAND_PRISON")
    assertThat(snapshot.deviceWearer?.firstName).isEqualTo(source.deviceWearer?.firstName)
    assertThat(snapshot.type).isEqualTo(type)
    assertThat(
      snapshot.addresses.map {
        it.addressLine1
      },
    ).containsExactlyElementsOf(source.addresses.map { it.addressLine1 })
    assertThat(snapshot.addresses.map { it.id }).doesNotContainAnyElementsOf(source.addresses.map { it.id })
    assertThat(snapshot.addresses.map { it.versionId }).containsOnly(snapshot.versionId)
    assertThat(repo.findById(id).orElseThrow().versions.single().versionId).isZero()
    if (state == SimulatedOrderState.DRAFT) {
      assertThat(created.simulation.caseId).isNull()
      assertThat(created.simulation.simulatedSubmissionVersionId).isNull()
    } else {
      assertThat(created.simulation.caseId).isEqualTo("sim-${snapshot.versionId}")
    }
    assertThat(getOrder(source.id).deviceWearer?.id).isEqualTo(source.deviceWearer?.id)
    when (state) {
      SimulatedOrderState.DRAFT -> {
        assertThat(snapshot.status).isEqualTo(OrderStatus.IN_PROGRESS)
        assertThat(snapshot.fmsResultId).isNull()
        assertThat(snapshot.fmsResultDate).isNull()
        assertThat(snapshot.submittedBy).isNull()
        assertThat(snapshot.statusUpdates).isEmpty()
        assertThat(created.returnEventId).isNull()
        val submitted = webTestClient.post().uri("/api/orders/$id/submit")
          .headers(setAuthorisation(roles = roles + "ROLE_PRISON")).exchange().expectStatus().isOk
          .expectBody(OrderDto::class.java).returnResult().responseBody!!
        assertThat(submitted.status).isEqualTo(OrderStatus.SUBMITTED)
        assertThat(submitted.canAmend).isFalse()
      }
      SimulatedOrderState.PROCESSING -> {
        assertThat(snapshot.status).isEqualTo(OrderStatus.SUBMITTED)
        assertThat(snapshot.canAmend).isFalse()
        assertThat(snapshot.fmsResultId).isNotEqualTo(source.fmsResultId).isNotNull()
        assertThat(snapshot.submittedBy).isEqualTo("John Smith")
        assertThat(created.simulation.caseState).isEqualTo(CaseState.NEW)
      }
      SimulatedOrderState.ACCEPTED -> {
        assertThat(snapshot.status).isEqualTo(OrderStatus.SUBMITTED)
        assertThat(snapshot.canAmend).isTrue()
        assertThat(created.simulation.caseState).isEqualTo(CaseState.AWAITING_VALIDATION)
        webTestClient.post().uri("/api/orders/$id/amend")
          .headers(setAuthorisation(roles = roles + "ROLE_PRISON")).exchange().expectStatus().isOk
          .expectBody().jsonPath("$.outcome").isEqualTo("ACCEPTED")
      }
      SimulatedOrderState.RETURNED -> {
        assertThat(created.returnEventId).isNotNull()
        await().untilAsserted {
          val returned = getOrder(id)
          assertThat(returned.status).isEqualTo(OrderStatus.REJECTED)
          assertThat(returned.canAmend).isTrue()
          assertThat(returned.statusUpdates.single().statusUpdateReasons.single().details)
            .isEqualTo("Correct this test order")
        }
        val simulated = webTestClient.get().uri("/api/dev/up3-simulation/orders/$id")
          .headers(simulationAuthorisation()).exchange().expectStatus().isOk
          .expectBody(Up3SimulationDto::class.java).returnResult().responseBody!!
        assertThat(simulated.lastProcessedEventId).isEqualTo(created.returnEventId)
      }
    }
    assertThat(queue.dlqIsEmpty()).isTrue()
    emails.assertNothingSent()
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @Test
  fun `fresh draft copies every section with new identities and ignores supplied history and ownership`() {
    val sourceOrder = repo.findById(seedSource().id).orElseThrow()
    val versionId = sourceOrder.versionId
    sourceOrder.installationAppointment = InstallationAppointment(versionId = versionId)
    sourceOrder.variationDetails = VariationDetails(versionId = versionId, variationDate = ZonedDateTime.now())
    sourceOrder.mandatoryAttendanceConditions.add(
      MandatoryAttendanceConditions(
        versionId = versionId,
        startDate = ZonedDateTime.now(),
        purpose = "Test appointment",
        appointmentDay = "MONDAY",
        startTime = "09:00",
        endTime = "10:00",
        addressLine1 = "Test address",
        addressLine2 = "Test town",
      ),
    )
    sourceOrder.dapoClauses.add(Dapo(versionId = versionId, clause = "test clause", date = ZonedDateTime.now()))
    sourceOrder.offences.add(
      Offence(versionId = versionId, offenceType = "test offence", offenceDate = ZonedDateTime.now()),
    )
    sourceOrder.offenceAdditionalDetails =
      OffenceAdditionalDetails(versionId = versionId, additionalDetails = "test details")
    sourceOrder.detailsOfInstallation = DetailsOfInstallation(versionId = versionId, riskDetails = "test risk")
    sourceOrder.reject(ZonedDateTime.now(), listOf(RejectionReason("other", "Old return")))
    repo.save(sourceOrder)
    val source = getOrder(sourceOrder.id)
    val created = createSimulated(
      CreateSimulatedOrderDto(source, SimulatedOrderState.DRAFT, creationRequestId = UUID.randomUUID()),
    )
    val snapshot = getOrder(created.simulation.orderId)
    val before: JsonNode = mapper.valueToTree(source)
    val after: JsonNode = mapper.valueToTree(snapshot)
    val sections = listOf(
      "additionalDocuments", "addresses", "contactDetails", "curfewConditions", "curfewReleaseDateConditions",
      "curfewTimeTable", "deviceWearer", "deviceWearerResponsibleAdult", "enforcementZoneConditions",
      "installationAndRisk", "interestedParties", "probationDeliveryUnit", "mandatoryAttendanceConditions",
      "monitoringConditions", "monitoringConditionsAlcohol", "monitoringConditionsTrail", "variationDetails",
      "installationLocation", "installationAppointment", "orderParameters", "dapoClauses", "offences",
      "offenceAdditionalDetails", "mappa", "detailsOfInstallation",
    )
    sections.forEach { name ->
      val original = before.path(name)
      val copy = after.path(name)
      assertThat(original.isNull).describedAs("$name fixture must be populated").isFalse()
      val originals = if (original.isArray) original.toList() else listOf(original)
      val copies = if (copy.isArray) copy.toList() else listOf(copy)
      assertThat(originals).describedAs(name).isNotEmpty()
      assertThat(copies).describedAs(name).hasSize(originals.size)
      originals.zip(copies).forEach { (old, new) ->
        assertThat(new.get("id").asString()).describedAs(name).isNotEqualTo(old.get("id").asString())
        assertThat(new.get("versionId").asString()).describedAs(name).isEqualTo(snapshot.versionId.toString())
      }
    }
    assertThat(snapshot.status).isEqualTo(OrderStatus.IN_PROGRESS)
    assertThat(snapshot.statusUpdates).isEmpty()
    assertThat(snapshot.username).isEqualTo(testUser)
    assertThat(snapshot.ownerCohort).isNotEqualTo(source.ownerCohort)
    assertThat(snapshot.isValid).isEqualTo(sourceOrder.isValid)
    assertThat(getOrder(source.id).statusUpdates).hasSize(1)
  }

  @Test
  fun `rejects invalid creation without leaving an order or simulation marker`() {
    val source = seedSource()
    val count = repo.count()
    val simulationCount = simulations.count()
    val submissionCount = submissions.count()
    val path = "/api/dev/up3-simulation/orders"
    val invalidRequests = listOf(
      CreateSimulatedOrderDto(
        source.copy(deviceWearer = null, isValid = true),
        SimulatedOrderState.PROCESSING,
        creationRequestId = UUID.randomUUID(),
      ),
      CreateSimulatedOrderDto(source, SimulatedOrderState.RETURNED, creationRequestId = UUID.randomUUID()),
      CreateSimulatedOrderDto(
        source,
        SimulatedOrderState.RETURNED,
        listOf(SimulatedReturnReasonDto("other")),
        UUID.randomUUID(),
      ),
      CreateSimulatedOrderDto(
        source,
        SimulatedOrderState.DRAFT,
        listOf(SimulatedReturnReasonDto("deviceWearer")),
        UUID.randomUUID(),
      ),
      CreateSimulatedOrderDto(
        source,
        SimulatedOrderState.RETURNED,
        listOf(SimulatedReturnReasonDto("")),
        UUID.randomUUID(),
      ),
    )
    invalidRequests.forEach {
      webTestClient.post().uri(path).headers(simulationAuthorisation(roles + "ROLE_PRISON"))
        .contentType(MediaType.APPLICATION_JSON).bodyValue(it).exchange().expectStatus().isBadRequest
      assertThat(repo.count()).isEqualTo(count)
      assertThat(simulations.count()).isEqualTo(simulationCount)
      assertThat(submissions.count()).isEqualTo(submissionCount)
    }
    webTestClient.post().uri(path).headers(simulationAuthorisation())
      .contentType(MediaType.APPLICATION_JSON).bodyValue("""{"order":{},"state":"UNKNOWN"}""")
      .exchange().expectStatus().isBadRequest
    webTestClient.post().uri(path).headers(setAuthorisation(roles = emptyList()))
      .contentType(MediaType.APPLICATION_JSON).bodyValue(
        CreateSimulatedOrderDto(source, SimulatedOrderState.DRAFT, creationRequestId = UUID.randomUUID()),
      )
      .exchange().expectStatus().isForbidden
    assertThat(repo.count()).isEqualTo(count)
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @Test
  fun `availability uses the normal login and distinguishes missing permission`() {
    webTestClient.get().uri("/api/dev/up3-simulation/availability")
      .headers(simulationAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.available").isEqualTo(true)
      .jsonPath("$.code").isEqualTo("UP3_SIMULATION_AVAILABLE")
    webTestClient.get().uri("/api/dev/up3-simulation/availability")
      .headers(simulationAuthorisation(emptyList())).exchange().expectStatus().isForbidden
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_PERMISSION_DENIED")
  }

  @Test
  fun `inspection and ordinary UI reads are read only and identify both versions explicitly`() {
    val created = seed(SimulatedOrderState.PROCESSING)
    val id = created.simulation.orderId
    val initial = getOrder(id)
    repeat(3) {
      val snapshot = inspect(id)
      assertThat(snapshot.currentOrderVersionId).isEqualTo(initial.versionId)
      assertThat(snapshot.simulatedSubmissionVersionId).isEqualTo(initial.versionId)
      assertThat(snapshot.currentOrderVersionNumber).isZero()
      assertThat(snapshot.simulatedSubmissionVersionNumber).isZero()
      assertThat(snapshot.isOwner).isTrue()
      assertThat(snapshot.canControlSubmission).isTrue()
      assertThat(snapshot.canAmend).isFalse()
      assertThat(getOrder(id).statusUpdates).isEmpty()
    }
    val persisted = repo.findById(id).orElseThrow()
    assertThat(persisted.statusUpdates).isEmpty()
    assertThat(persisted.lastUpdatedBy).isEqualTo(initial.lastUpdatedBy)
    assertThat(persisted.lastUpdatedDateTime).isEqualTo(initial.lastUpdatedDateTime)
    webTestClient.get().uri(simulationPath(id)).headers(simulationAuthorisation())
      .exchange().expectStatus().isOk.expectBody().jsonPath("$.versionId").doesNotExist()
  }

  @Test
  fun `state controls enforce expected version identities with structured conflicts`() {
    val wrong = UUID.randomUUID()

    val created = seed(SimulatedOrderState.PROCESSING)
    val id = created.simulation.orderId
    val snapshot = inspect(id)
    webTestClient.put().uri("${simulationPath(id)}/state").headers(simulationAuthorisation())
      .bodyValue(
        UpdateSimulatedCaseStateDto(
          SimulatedCaseState.ACCEPTED,
          wrong,
          requireNotNull(snapshot.simulatedSubmissionVersionId),
        ),
      ).exchange().expectStatus().isEqualTo(409)
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_STALE_VERSION")
      .jsonPath("$.expectedCurrentOrderVersionId").isEqualTo(wrong.toString())
      .jsonPath("$.currentOrderVersionId").isEqualTo(snapshot.currentOrderVersionId.toString())
    webTestClient.put().uri("${simulationPath(id)}/state").headers(simulationAuthorisation())
      .bodyValue(
        UpdateSimulatedCaseStateDto(SimulatedCaseState.ACCEPTED, snapshot.currentOrderVersionId, wrong),
      ).exchange().expectStatus().isEqualTo(409)
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_STALE_VERSION")
      .jsonPath("$.simulatedSubmissionVersionId").isEqualTo(snapshot.simulatedSubmissionVersionId.toString())
    assertThat(inspect(id).caseState).isEqualTo(CaseState.NEW)
    assertThat(getOrder(id).statusUpdates).isEmpty()
  }

  @Test
  fun `not enrolled and missing orders have distinct outcomes`() {
    val order = createStoredOrder()
    webTestClient.get().uri(simulationPath(order.id)).headers(simulationAuthorisation())
      .exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_NOT_ENROLLED")
    webTestClient.get().uri(simulationPath(UUID.randomUUID())).headers(simulationAuthorisation())
      .exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_ORDER_NOT_FOUND")
  }

  @Test
  fun `identical return retries preserve one intent and one committed rejection`() {
    val created = seed(SimulatedOrderState.PROCESSING)
    val id = created.simulation.orderId
    val request = returnRequest(inspect(id))
    requestReturn(id, request)
    requestReturn(id, request)
    await().untilAsserted {
      assertThat(getReturnEvent(id, request.eventId).status).isEqualTo(SimulatedReturnStatus.PROCESSED)
    }
    val before = inspect(id)
    requestReturn(id, request)
    val after = inspect(id)
    assertThat(getOrder(id).statusUpdates).hasSize(1)
    assertThat(after.capturedUserEmails).isEqualTo(before.capturedUserEmails)
    assertThat(after.capturedNoEmails).isEqualTo(before.capturedNoEmails)
    assertThat(getReturnEvent(id, request.eventId).publicationAttempts).isEqualTo(1)
    assertThat(returns.findAll().count { it.orderId == id }).isEqualTo(1)
    webTestClient.post().uri("${simulationPath(id)}/return").headers(simulationAuthorisation())
      .bodyValue(request.copy(reasons = listOf(SimulatedReturnReasonDto("other", "Different return"))))
      .exchange().expectStatus().isEqualTo(409)
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_EVENT_ID_CONFLICT")
    webTestClient.post().uri("${simulationPath(id)}/return").headers {
      setAuthorisation(username = "another-user", roles = roles + "ROLE_PRISON")(it)
    }.bodyValue(request).exchange().expectStatus().isForbidden
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_PERMISSION_DENIED")
    val other = createStoredOrder()
    webTestClient.get().uri("${simulationPath(other.id)}/returns/${request.eventId}")
      .headers(simulationAuthorisation()).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_EVENT_NOT_FOUND")
    emails.assertNothingSent()
  }

  @ParameterizedTest
  @ValueSource(booleans = [false, true])
  fun `delayed returns are stale ignored after normal amendment or resubmission`(resubmit: Boolean) {
    val created = seed(SimulatedOrderState.ACCEPTED)
    val id = created.simulation.orderId
    val original = inspect(id)
    val request = returnRequest(original)
    doNothing().whenever(publisher).publish(request.eventId)
    requestReturn(id, request)
    assertThat(getReturnEvent(id, request.eventId).status).isEqualTo(SimulatedReturnStatus.PENDING_PUBLICATION)
    webTestClient.post().uri("/api/orders/$id/amend")
      .headers(setAuthorisation(roles = roles + "ROLE_PRISON")).exchange().expectStatus().isOk
    val draft = inspect(id)
    assertThat(draft.currentOrderVersionId).isNotEqualTo(original.currentOrderVersionId)
    assertThat(draft.simulatedSubmissionVersionId).isEqualTo(original.simulatedSubmissionVersionId)
    assertThat(draft.canControlSubmission).isFalse()
    webTestClient.put().uri("${simulationPath(id)}/state").headers(simulationAuthorisation())
      .bodyValue(
        UpdateSimulatedCaseStateDto(
          SimulatedCaseState.PROCESSING,
          original.currentOrderVersionId,
          requireNotNull(original.simulatedSubmissionVersionId),
        ),
      ).exchange().expectStatus().isEqualTo(409)
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_STALE_VERSION")
    if (resubmit) {
      val order = repo.findById(id).orElseThrow()
      order.orderParameters?.haveLicence = true
      order.variationDetails = VariationDetails(
        versionId = order.versionId,
        variationDate = ZonedDateTime.now(),
        variationDetails = "Test resubmission",
      )
      repo.save(order)
      webTestClient.post().uri("/api/orders/$id/submit")
        .headers(setAuthorisation(roles = roles + "ROLE_PRISON")).exchange().expectStatus().isOk
      assertThat(inspect(id).simulatedSubmissionVersionId).isEqualTo(draft.currentOrderVersionId)
    }
    doCallRealMethod().whenever(publisher).publish(request.eventId)
    publisher.publish(request.eventId)
    await().untilAsserted {
      assertThat(getReturnEvent(id, request.eventId).status).isEqualTo(SimulatedReturnStatus.STALE_IGNORED)
    }
    requestReturn(id, request)
    val latest = getOrder(id)
    assertThat(latest.status).isEqualTo(if (resubmit) OrderStatus.SUBMITTED else OrderStatus.IN_PROGRESS)
    assertThat(latest.statusUpdates).isEmpty()
    val history = repo.findById(id).orElseThrow().getSpecificVersion(original.currentOrderVersionId)!!
    assertThat(history.status).isEqualTo(OrderStatus.SUBMITTED)
    assertThat(history.statusUpdates.map { it.status.name }).containsExactly("ACCEPTED")
    assertThat(inspect(id).capturedUserEmails).isZero()
    assertThat(queue.dlqIsEmpty()).isTrue()
    emails.assertNothingSent()
    assertThat(sercoApi.allServeEvents).isEmpty()
    assertThat(sercoAuthApi.allServeEvents).isEmpty()
  }

  @Test
  fun `normal amendment waits for the simulator order guard and evaluates the committed state`() {
    val created = seed(SimulatedOrderState.ACCEPTED)
    val id = created.simulation.orderId
    val version = created.simulation.currentOrderVersionId
    val started = CountDownLatch(1)
    lateinit var amendment: CompletableFuture<Int>
    TransactionTemplate(transactionManager).executeWithoutResult {
      repo.lockById(id)
      amendment = CompletableFuture.supplyAsync {
        started.countDown()
        webTestClient.post().uri("/api/orders/$id/amend")
          .headers(setAuthorisation(roles = roles + "ROLE_PRISON"))
          .exchange().returnResult(String::class.java).status.value()
      }
      assertThat(started.await(1, TimeUnit.SECONDS)).isTrue()
      await().during(Duration.ofMillis(200)).atMost(Duration.ofSeconds(1)).until { !amendment.isDone }
      val token = AuthAwareAuthenticationToken(
        Jwt.withTokenValue("test").header("alg", "none").claim("user_name", testUser).build(),
        testUser,
        "John Smith",
        roles.map { SimpleGrantedAuthority(it) },
      )
      controls.setState(id, token, UpdateSimulatedCaseStateDto(SimulatedCaseState.PROCESSING, version, version))
    }
    assertThat(amendment.get(5, TimeUnit.SECONDS)).isEqualTo(409)
    assertThat(repo.findById(id).orElseThrow().versions).hasSize(1)
    assertThat(inspect(id).caseState).isEqualTo(CaseState.NEW)
  }

  @Test
  fun `processing failures survive rollback and successful redelivery commits effects once`() {
    val created = seed(SimulatedOrderState.PROCESSING)
    val id = created.simulation.orderId
    val request = returnRequest(inspect(id))
    val caseId = requireNotNull(inspect(id).caseId)
    doThrow(IllegalStateException("notification construction failed"))
      .whenever(rejection).execute(org.mockito.kotlin.eq(caseId), org.mockito.kotlin.any(), org.mockito.kotlin.any())
    requestReturn(id, request)
    await().untilAsserted {
      val failed = getReturnEvent(id, request.eventId)
      assertThat(failed.status).isEqualTo(SimulatedReturnStatus.FAILED)
      assertThat(failed.failureStage?.name).isEqualTo("PROCESSING")
      assertThat(failed.processingFailures).isGreaterThanOrEqualTo(1)
    }
    assertThat(getOrder(id).status).isEqualTo(OrderStatus.SUBMITTED)
    assertThat(getOrder(id).statusUpdates).isEmpty()
    doCallRealMethod().whenever(rejection)
      .execute(org.mockito.kotlin.eq(caseId), org.mockito.kotlin.any(), org.mockito.kotlin.any())
    val message = returns.findById(request.eventId).orElseThrow().payload
    translator.processEvent(message)
    translator.processEvent(message)
    assertThat(getReturnEvent(id, request.eventId).status).isEqualTo(SimulatedReturnStatus.PROCESSED)
    assertThat(getOrder(id).statusUpdates).hasSize(1)
    assertThat(inspect(id).capturedUserEmails).isEqualTo(1)
    emails.assertNothingSent()
  }

  @Test
  fun `creation recovery and identical replay retain the original order and return identity`() {
    val source = seedSource()
    val request = CreateSimulatedOrderDto(
      source,
      SimulatedOrderState.RETURNED,
      listOf(SimulatedReturnReasonDto("other", "Correct this order")),
      UUID.randomUUID(),
    )
    val created = createSimulated(request)
    val id = created.simulation.orderId
    val count = repo.count()
    val returnId = requireNotNull(created.returnEventId)
    await().untilAsserted { assertThat(getReturnEvent(id, returnId).status).isEqualTo(SimulatedReturnStatus.PROCESSED) }
    webTestClient.get().uri("/api/dev/up3-simulation/creations/${request.creationRequestId}")
      .headers(simulationAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.status").isEqualTo("CREATED")
      .jsonPath("$.orderId").isEqualTo(id.toString())
      .jsonPath("$.createdOrderVersionId").isEqualTo(created.createdOrderVersionId.toString())
      .jsonPath("$.returnEventId").isEqualTo(returnId.toString())
    val replay = createSimulated(
      request.copy(order = source.copy(id = UUID.randomUUID(), username = "ignored", isValid = true, canAmend = true)),
    )
    assertThat(replay.simulation.orderId).isEqualTo(id)
    assertThat(replay.returnEventId).isEqualTo(returnId)
    assertThat(repo.count()).isEqualTo(count)
    assertThat(getOrder(id).statusUpdates).hasSize(1)
    assertThat(getReturnEvent(id, returnId).publicationAttempts).isEqualTo(1)
    webTestClient.post().uri("/api/dev/up3-simulation/orders").headers(simulationAuthorisation(roles + "ROLE_PRISON"))
      .bodyValue(request.copy(state = SimulatedOrderState.DRAFT, reasons = emptyList()))
      .exchange().expectStatus().isEqualTo(409)
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_CREATION_ID_CONFLICT")
    webTestClient.get().uri("/api/dev/up3-simulation/creations/${request.creationRequestId}")
      .headers {
        setAuthorisation(username = "another-user", roles = roles + "ROLE_PRISON")(it)
      }.exchange().expectStatus().isForbidden
    assertThat(repo.count()).isEqualTo(count)
  }

  @RepeatedTest(3)
  fun `concurrent identical creation requests commit only one order`() {
    val request = CreateSimulatedOrderDto(
      seedSource(),
      SimulatedOrderState.DRAFT,
      creationRequestId = UUID.randomUUID(),
    )
    val before = repo.count()
    val start = CountDownLatch(1)
    val attempts = (1..2).map {
      CompletableFuture.supplyAsync {
        check(start.await(5, TimeUnit.SECONDS))
        createSimulated(request)
      }
    }
    start.countDown()
    val created = attempts.map { it.get(10, TimeUnit.SECONDS) }
    assertThat(created.map { it.simulation.orderId }.distinct()).hasSize(1)
    assertThat(repo.count()).isEqualTo(before + 1)
    assertThat(creations.findAll().count { it.creationRequestId == request.creationRequestId }).isEqualTo(1)
  }

  @Test
  fun `concurrent returns on different orders cannot overwrite a shared event identity`() {
    val orders = listOf(seed(SimulatedOrderState.PROCESSING), seed(SimulatedOrderState.PROCESSING))
    val id = UUID.randomUUID()
    val start = CountDownLatch(1)
    val attempts = orders.map {
      val orderId = it.simulation.orderId
      val request = returnRequest(inspect(orderId)).copy(eventId = id)
      CompletableFuture.supplyAsync {
        check(start.await(5, TimeUnit.SECONDS))
        webTestClient.post().uri("${simulationPath(orderId)}/return").headers(simulationAuthorisation())
          .bodyValue(request).exchange().returnResult(String::class.java).status.value()
      }
    }
    start.countDown()
    assertThat(attempts.map { it.get(10, TimeUnit.SECONDS) }).containsExactlyInAnyOrder(202, 409)
    val event = returns.findById(id).orElseThrow()
    val winner = orders.single { it.simulation.orderId == event.orderId }
    assertThat(event.targetOrderVersionId).isEqualTo(winner.simulation.currentOrderVersionId)
    assertThat(event.simulatedSubmissionVersionId).isEqualTo(winner.simulation.simulatedSubmissionVersionId)
    val other = orders.single { it.simulation.orderId != event.orderId }
    assertThat(getOrder(other.simulation.orderId).status).isEqualTo(OrderStatus.SUBMITTED)
    assertThat(getOrder(other.simulation.orderId).statusUpdates).isEmpty()
  }

  @Test
  fun `pending and absent creation recovery never creates an order automatically`() {
    val request = CreateSimulatedOrderDto(
      seedSource(),
      SimulatedOrderState.DRAFT,
      creationRequestId = UUID.randomUUID(),
    )
    creations.save(
      Up3SimulationCreation(
        request.creationRequestId,
        payloads.creationHash(request),
        testUser,
        SimulatedOrderState.DRAFT,
      ),
    )
    val count = repo.count()
    webTestClient.get().uri("/api/dev/up3-simulation/creations/${request.creationRequestId}")
      .headers(simulationAuthorisation()).exchange().expectStatus().isOk
      .expectBody().jsonPath("$.status").isEqualTo("PENDING").jsonPath("$.orderId").isEmpty()
    webTestClient.get().uri("/api/dev/up3-simulation/creations/${UUID.randomUUID()}")
      .headers(simulationAuthorisation()).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_CREATION_NOT_FOUND")
    assertThat(repo.count()).isEqualTo(count)
    val first = createSimulated(request)
    assertThat(createSimulated(request).simulation.orderId).isEqualTo(first.simulation.orderId)
    assertThat(repo.count()).isEqualTo(count + 1)
  }

  @Test
  fun `creation identity survives deletion and replay never recreates the test order`() {
    val request = CreateSimulatedOrderDto(
      seedSource(),
      SimulatedOrderState.DRAFT,
      creationRequestId = UUID.randomUUID(),
    )
    val created = createSimulated(request)
    repo.deleteById(created.simulation.orderId)
    val count = repo.count()
    webTestClient.post().uri("/api/dev/up3-simulation/orders").headers(simulationAuthorisation())
      .bodyValue(request).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("$.errorCode").isEqualTo("UP3_SIMULATION_CREATED_ORDER_REMOVED")
    assertThat(repo.count()).isEqualTo(count)
  }

  private fun simulationPath(id: UUID) = "/api/dev/up3-simulation/orders/$id"

  private fun inspect(id: UUID): Up3SimulationDto = webTestClient.get().uri(simulationPath(id))
    .headers(simulationAuthorisation()).exchange().expectStatus().isOk
    .expectBody(Up3SimulationDto::class.java).returnResult().responseBody!!

  private fun getReturnEvent(id: UUID, eventId: UUID): SimulatedReturnEventDto =
    webTestClient.get().uri("${simulationPath(id)}/returns/$eventId").headers(simulationAuthorisation())
      .exchange().expectStatus().isOk.expectBody(SimulatedReturnEventDto::class.java).returnResult().responseBody!!

  private fun returnRequest(snapshot: Up3SimulationDto) = SimulateReturnDto(
    UUID.randomUUID(),
    Instant.now(),
    listOf(SimulatedReturnReasonDto("other", "Correct this order")),
    snapshot.currentOrderVersionId,
    requireNotNull(snapshot.simulatedSubmissionVersionId),
  )

  private fun requestReturn(id: UUID, request: SimulateReturnDto): SimulatedReturnEventDto =
    webTestClient.post().uri("${simulationPath(id)}/return").headers(simulationAuthorisation())
      .bodyValue(request).exchange().expectStatus().isAccepted
      .expectBody(SimulatedReturnEventDto::class.java).returnResult().responseBody!!

  private fun seed(state: SimulatedOrderState) = createSimulated(
    CreateSimulatedOrderDto(seedSource(), state, creationRequestId = UUID.randomUUID()),
  )

  private fun seedSource(type: RequestType = RequestType.REQUEST): OrderDto {
    manageUserApi.stubUserActiveCaseLoad(
      HmppsUserCaseloadResponse(active = true, activeCaseload = HmppsCaseload("CADM_I", "ADMIN")),
    )
    manageUserApi.stubGetUserEmail(HmppsUserEmailResponse(testUser, "test@example.com", true))
    val source = TestUtilities.createReadyToSubmitOrder(requestType = type)
    source.lastUpdatedBy = "Original updater"
    source.lastUpdatedDateTime = OffsetDateTime.now().minusDays(1)
    source.ownerCohort = "Original cohort"
    repo.save(source)
    source.additionalDocuments.forEach { documentApi.stubGetDocument(it.documentId.toString()) }
    source.enforcementZoneConditions.filter { it.fileId != null }.forEach {
      documentApi.stubGetDocument(it.fileId.toString())
    }
    return getOrder(source.id).copy(
      username = "IGNORE_THIS_OWNER",
      ownerCohort = "IGNORE_THIS_COHORT",
      submittedBy = "IGNORE_THIS_SUBMITTER",
      fmsResultId = UUID.randomUUID(),
      fmsResultDate = OffsetDateTime.now().minusDays(1),
      status = OrderStatus.SUBMITTED,
      isValid = false,
    )
  }

  private fun createSimulated(request: CreateSimulatedOrderDto): CreatedSimulatedOrderDto =
    webTestClient.post().uri("/api/dev/up3-simulation/orders")
      .headers(simulationAuthorisation(roles + "ROLE_PRISON"))
      .contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated
      .expectBody(CreatedSimulatedOrderDto::class.java).returnResult().responseBody!!

  private fun simulationAuthorisation(userRoles: List<String> = roles): (HttpHeaders) -> Unit =
    setAuthorisation(roles = userRoles)
}
