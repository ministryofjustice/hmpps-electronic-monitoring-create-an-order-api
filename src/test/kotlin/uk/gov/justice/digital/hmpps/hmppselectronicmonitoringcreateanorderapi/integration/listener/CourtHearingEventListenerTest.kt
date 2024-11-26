package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.listener

import io.micrometer.core.instrument.util.StringEscapeUtils
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times
import org.mockito.kotlin.any
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository
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
  lateinit var repo: SubmitFmsOrderResultRepository

  @SpyBean
  lateinit var eventHandler: HearingEventHandler

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

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
    verify(eventHandler, Times(0)).handleHearingEvent(any())
  }

  @Test
  fun `Will not process a Scotland community order hearing event`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/Community_order_Scotland.json")
    sendDomainSqsMessage(rawMessage)
    await().until { getNumberOfMessagesCurrentlyOnEventQueue() == 0 }
    verify(eventHandler, Times(0)).handleHearingEvent(any())
  }

  fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

  @Test
  fun `Will map COEW AAR request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/COEW_AAR"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/SSO_YOUNG_OFFENDER_INSTITUTION_DETENTION"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map SSO_INPRISONMENT request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/SSO_INPRISONMENT"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map REMCB_BAIL_CURFEW request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/REMCB_BAIL_CURFEW"
    runPayloadTest(rootFilePath)
  }

  @Test
  fun `Will map CCSIB_BAIL_EXCLUSION request and submit to FMS`() {
    val rootFilePath = "src/test/resources/json/CCSIB_BAIL_EXCLUSION"
    runPayloadTest(rootFilePath)
  }

  fun runPayloadTest(rootFilePath: String) {
    val rawMessage = generateRawHearingEventMessage("$rootFilePath/cp_payload.json")
    sercoApi.stupCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stupMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )
    sendDomainSqsMessage(rawMessage)
    await().until { repo.count().toInt() != 0 || getNumberOfMessagesCurrentlyOnDeadLetterQueue() != 0 }
    assertThat(getNumberOfMessagesCurrentlyOnDeadLetterQueue()).isEqualTo(0)
    val savedResult = repo.findAll().first()
    assertThat(savedResult).isNotNull
    val mockDeviceWearerJson = Files.readString(Paths.get("$rootFilePath/expected_fms_device_wearer.json"))
    val mockOrderJson = Files.readString(
      Paths.get("$rootFilePath/expected_fms_order.json"),
    ).replace("{expectedOderId}", savedResult.id.toString())
    assertThat(savedResult.fmsDeviceWearerRequest).isEqualTo(mockDeviceWearerJson.removeWhitespaceAndNewlines())
    assertThat(savedResult.fmsOrderRequest).isEqualTo(mockOrderJson.removeWhitespaceAndNewlines())
    assertThat(savedResult.error).isEqualTo("")
    assertThat(savedResult.success).isTrue()
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
