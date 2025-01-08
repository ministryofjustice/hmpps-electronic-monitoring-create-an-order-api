package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.listener

import com.microsoft.applicationinsights.TelemetryClient
import io.micrometer.core.instrument.util.StringEscapeUtils
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.CourtHearingEventListener
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

@Transactional
class CourtHearingEventListenerTest : IntegrationTestBase() {
  @SpyBean
  lateinit var repo: FmsSubmissionResultRepository

  @SpyBean
  lateinit var eventHandler: HearingEventHandler

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var courtHearingEventListener: CourtHearingEventListener

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  val courtHearingEventQueueConfig by lazy {
    hmppsQueueService.findByQueueId("courthearingeventqueue")
      ?: throw MissingQueueException("HmppsQueue courthearingeventqueue not found")
  }
  val courtHearingEventQueueSqsUrl by lazy { courtHearingEventQueueConfig.queueUrl }
  val courtHearingEventQueueSqsClient by lazy { courtHearingEventQueueConfig.sqsClient }
  val courtHearingEventDeadLetterSqsClient by lazy { courtHearingEventQueueConfig.sqsDlqClient as SqsAsyncClient }
  val courtHearingEventDeadLetterSqsUrl by lazy { courtHearingEventQueueConfig.dlqUrl as String }

  @BeforeEach
  fun setup() {
    courtHearingEventQueueSqsClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(courtHearingEventQueueSqsUrl).build(),
    ).get()
    courtHearingEventDeadLetterSqsClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(courtHearingEventDeadLetterSqsUrl).build(),
    ).get()
    sercoAuthApi.stubGrantToken()
    repo.deleteAll()
  }

  @Test
  fun `Will not process a malformed court hearing event and dead letter hearing event`() {
    sendDomainSqsMessage("BAD JSON")
    await().until { getNumberOfMessagesCurrentlyOnDeadLetterQueue() == 1 }
    val deadLetterQueueMessage = geMessagesCurrentlyOnDeadLetterQueue()
    val message = deadLetterQueueMessage.messages().first()
    assertThat(message.body()).isEqualTo("BAD JSON")
    assertThat(
      message.messageAttributes()["Error"]!!.stringValue(),
    ).isEqualTo("Malformed event received. Could not parse JSON")
    val savedEvent = repo.findAll().firstOrNull()
    assertThat(savedEvent).isNull()
    verify(eventHandler, Times(0)).handleHearingEvent(any())
  }

  @Test
  fun `Will not process a hearing event has no EM request`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/No_EM_Payload.json")
    sendDomainSqsMessage(rawMessage)
    await().until { getNumberOfMessagesCurrentlyOnEventQueue() == 0 }
  }

  @Test
  fun `Will not process a Scotland community order hearing event`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/Community_order_Scotland.json")
    sendDomainSqsMessage(rawMessage)
    await().until { getNumberOfMessagesCurrentlyOnEventQueue() == 0 }
    verify(eventHandler, Times(0)).handleHearingEvent(any())
  }

  @Test
  fun `Will process a valid payload with em details`() {
    val rootFilePath = "src/test/resources/json/SUSPS_community_order_inclusion"
    val rawMessage = generateRawHearingEventMessage("$rootFilePath/cp_payload.json")
    sercoApi.stubCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stubCreateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )
    sendDomainSqsMessage(rawMessage)
    await().until { getNumberOfMessagesCurrentlyOnEventQueue() == 0 }
    assertThat(repo.count().toInt()).isNotEqualTo(0)
  }

  fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

  @Test
  fun `Will map COEW_community_order_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/COEW_community_order_curfew"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map COEW_community_order_alcohol request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/COEW_community_order_alcohol"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map SUSPSD_community_order_exclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/SUSPSD_community_order_exclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map SUSPS_community_order_inclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/SUSPS_community_order_inclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map COV_community_order_trail and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/COV_community_order_trail"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map REMCB_bail_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/REMCB_bail_curfew"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCSIB_bail_exclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCSIB_bail_exclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCSIB_crown_court_bail_exclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCSIB_crown_court_bail_exclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCSIB_crown_court_week_commencing_next_hearing_date request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCSIB_crown_court_week_commencing_next_hearing_date"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCSIB_crown_court_no_fixed_next_hearing_date request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCSIB_crown_court_no_fixed_next_hearing_date"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map RILAB_bail_inclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/RILAB_bail_inclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map RIB_bail_exclusion_except_court_or_appointment request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/RIB_bail_exclusion_except_court_or_appointment"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map RC_bail_inclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/RC_bail_inclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map RCCLAB_bail_exclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/RCCLAB_bail_exclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCIB_bail_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCIB_bail_curfew"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCIC_bail_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCIC_bail_curfew"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map YROEW_youth_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/YROEW_youth_curfew"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map YROFEW_youth_trail request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/YROFEW_youth_trail"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map YROISS_youth_exclusion request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/YROISS_youth_exclusion"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map SDO_supervision_curfew request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/SDO_supervision_curfew"
    runPayloadTest(rootFilePath)
  }

  fun runPayloadTest(rootFilePath: String) {
    val rawMessage = generateRawHearingEventMessage("$rootFilePath/cp_payload.json")
    sercoApi.stubCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stubCreateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )
    courtHearingEventListener.onDomainEvent(rawMessage)
    assertThat(getNumberOfMessagesCurrentlyOnDeadLetterQueue()).isEqualTo(0)
    val savedResult = repo.findAll().first()
    assertThat(savedResult).isNotNull
    val mockDeviceWearerJson = Files.readString(Paths.get("$rootFilePath/expected_fms_device_wearer.json"))
    val mockOrderJson = Files.readString(
      Paths.get("$rootFilePath/expected_fms_order.json"),
    ).replace("{expectedOderId}", savedResult.orderId.toString())
    assertThat(savedResult.deviceWearerResult.payload).isEqualTo(mockDeviceWearerJson.removeWhitespaceAndNewlines())
    assertThat(savedResult.monitoringOrderResult.payload).isEqualTo(mockOrderJson.removeWhitespaceAndNewlines())
    assertThat(savedResult.error).isEqualTo("")
    assertThat(savedResult.success).isTrue()
  }

  @Test
  fun `Will log event for malformed payload`() {
    courtHearingEventListener.onDomainEvent("BAD JSON")

    verify(telemetryClient, Times(1)).trackEvent(
      eq("Common_Platform_Exception"),
      argThat { it -> it.containsKey("Start Date And Time") },
      argThat { it -> it.containsKey("eventTimeMs") },
    )
  }

  @Test
  fun `Will log ignored request event has no EM request`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/No_EM_Payload.json")
    courtHearingEventListener.onDomainEvent(rawMessage)
    verify(telemetryClient, Times(1)).trackEvent(
      eq("Common_Platform_Ignored_Request"),
      argThat { it ->
        it.containsKey("Contain notification of electronic monitoring order label") &&
          it.containsKey("Start Date And Time")
      },
      argThat { it -> it.containsKey("eventTimeMs") },
    )
  }

  @Test
  fun `Will log success and request event for valid em payload`() {
    val rawMessage =
      generateRawHearingEventMessage("src/test/resources/json/COEW_community_order_alcohol/cp_payload.json")
    courtHearingEventListener.onDomainEvent(rawMessage)

    verify(telemetryClient, Times(1)).trackEvent(
      eq("Common_Platform_Success_Request"),
      argThat { it ->
        it.containsKey("OrderType") &&
          it.containsKey("Start Date And Time")
      },
      argThat { it -> it.containsKey("eventTimeMs") },
    )
  }

  fun generateRawHearingEventMessage(path: String): String {
    val content = Files.readString(Paths.get(path))
    return """
      {
        "Type" : "Notification",
        "MessageId" : "eed5fdf9-ea08-5bf2-9d96-a27aed48bb71",
        "TopicArn" : "arn:aws:sns:eu-west-2:754256621582:cloud-platform-probation-in-court-team-5b4824dca700d8b3ec75f25d24adfbb9",
        "Message" : "${StringEscapeUtils.escapeJson(content)}",
        "Timestamp" : "2024-10-11T10:59:12.671Z",
        "SignatureVersion" : "1",
        "Signature" : "ZiRmEPybVu+zrg/N7sJh0il8DevPbu+8jFNIB8K7NmmLbgsSOOMLy2tNAxWrwJBdTZa3MIBW+zFlaKrSFFVE1mgpKVT3j1M11B0U8CZPQweS1zE8cGRUDtz9V1P02+mufvmKA5KD5mJmU40jy/PhR8stK6NUqoEW2Eycppgn7LDBDS3UiT1761hxUy67tCA/R8DGAEGhaJ+0dMMGqZKYh5ImCYTom5oNv6RWzsqR741HLQDtAuXDfEwAs9m6JS4B88DMouh/PuXW7dkQV+Jn+xR4HDFc+FcYppW91RHPthrMGl6my+n3/C6d81Kn9bTWMdeoHsModjnmmd2LVItV6w==",
        "SigningCertURL" : "https://sns.eu-west-2.amazonaws.com/SimpleNotificationService-60eadc530605d63b8e62a523676ef735.pem",
        "UnsubscribeURL" : "https://sns.eu-west-2.amazonaws.com/?Action=Unsubscribe%26SubscriptionArn=arn:aws:sns:eu-west-2:754256621582:cloud-platform-probation-in-court-team-5b4824dca700d8b3ec75f25d24adfbb9:6e8b52e0-4404-4dcb-9449-19b11705eb9c",
        "MessageAttributes" : {
          "messageType" : {"Type":"String","Value":"COMMON_PLATFORM_HEARING"},
          "hearingEventType" : {"Type":"String","Value":"Unknown"}
        }
      }
    """.trimIndent()
  }

  fun getNumberOfMessagesCurrentlyOnEventQueue(): Int = courtHearingEventQueueSqsClient.countAllMessagesOnQueue(
    courtHearingEventQueueSqsUrl,
  ).get()

  fun getNumberOfMessagesCurrentlyOnDeadLetterQueue(): Int =
    courtHearingEventDeadLetterSqsClient.countAllMessagesOnQueue(
      courtHearingEventDeadLetterSqsUrl,
    ).get()

  fun geMessagesCurrentlyOnDeadLetterQueue(): ReceiveMessageResponse =
    courtHearingEventDeadLetterSqsClient.receiveMessage(
      ReceiveMessageRequest.builder().queueUrl(courtHearingEventDeadLetterSqsUrl).messageAttributeNames("All").build(),
    ).get()

  fun sendDomainSqsMessage(rawMessage: String): CompletableFuture<SendMessageResponse> =
    courtHearingEventQueueSqsClient.sendMessage(
      SendMessageRequest.builder().queueUrl(courtHearingEventQueueSqsUrl).messageBody(rawMessage).build(),
    )
}
