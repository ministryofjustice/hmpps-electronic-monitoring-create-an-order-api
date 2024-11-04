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
  fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

  @Test
  fun `Will map COEW AAR request and submit to FMS`() {
    val rawMessage = generateRawHearingEventMessage("src/test/resources/json/COEW_AAR.json")

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

    val mockDeviceWearerJson = """
      {
      "title": "",
      "first_name": "Janie",
      "middle_name": "",
      "last_name": "Ernser",
      "alias": null,
      "date_of_birth": "1991-02-18",
      "adult_child": "adult",
      "sex": "MALE",
      "gender_identity": "MALE",
      "disability": [],
      "address_1": "21",
      "address_2": "Furnburry Park Road",
      "address_3": "London",
      "address_4": "N/A",
      "address_post_code": "SW11 3TQ",
      "secondary_address_1": "",
      "secondary_address_2": "",
      "secondary_address_3": "",
      "secondary_address_4": "",
      "secondary_address_post_code": "",
      "phone_number": "01472544375",
      "risk_serious_harm": null,
      "risk_self_harm": null,
      "risk_details": null,
      "mappa": null,
      "mappa_case_type": null,
      "risk_categories": [],
      "responsible_adult_required": "false",
      "parent": "null",
      "guardian": "",
      "parent_address_1": "",
      "parent_address_2": "",
      "parent_address_3": "",
      "parent_address_4": "",
      "parent_address_post_code": "",
      "parent_phone_number": null,
      "parent_dob": "",
      "pnc_id": null,
      "nomis_id": null,
      "delius_id": null,
      "prison_number": null,
      "home_office_case_reference_number": null,
      "interpreter_required": null,
      "language": null
    }
     """
    val mockOrderJson = """
      {
	"case_id": "MockDeviceWearerId",
	"allday_lockdown": "",
	"atv_allowance": "",
	"condition_type": "Requirement of a Community Order",
	"court": "",
	"court_order_email": "",
	"describe_exclusion": "",
	"device_type": null,
	"device_wearer": "Janie Ernser",
	"enforceable_condition": [
		{
			"condition": "AAMR"
		}
	],
	"exclusion_allday": "",
	"interim_court_date": "",
	"issuing_organisation": "",
	"media_interest": "",
	"new_order_received": "",
	"notifying_officer_email": "",
	"notifying_officer_name": "",
	"notifying_organization": "Lavender Hill Magistrates' Court",
	"no_post_code": "",
	"no_address_1": "",
	"no_address_2": "",
	"no_address_3": "",
	"no_address_4": "",
	"no_email": "",
	"no_name": "",
	"no_phone_number": "",
	"offence": "",
	"offence_date": "",
	"order_end": "2025-12-12",
	"order_id": "${savedResult.id}",
	"order_request_type": "",
	"order_start": "2024-10-21",
	"order_type": "Community",
	"order_type_description": null,
	"order_type_detail": "",
	"order_variation_date": "",
	"order_variation_details": "",
	"order_variation_req_received_date": "",
	"order_variation_type": "",
	"pdu_responsible": "",
	"pdu_responsible_email": "",
	"planned_order_end_date": "",
	"responsible_officer_details_received": "",
	"responsible_officer_email": "",
	"responsible_officer_phone": null,
	"responsible_officer_name": "",
	"responsible_organization": "Probation",
	"ro_post_code": "SW11 1JU",
	"ro_address_1": "",
	"ro_address_2": "",
	"ro_address_3": "",
	"ro_address_4": "",
	"ro_email": "londonnps.court@justice.gov.uk",
	"ro_phone": null,
	"ro_region": "London Division NPS",
	"sentence_date": "",
	"sentence_expiry": "",
	"tag_at_source": "",
	"tag_at_source_details": "",
	"technical_bail": "",
	"trial_date": "",
	"trial_outcome": "",
	"conditional_release_date": "",
	"reason_for_order_ending_early": "",
	"business_unit": "",
	"service_end_date": "2025-12-12",
	"curfew_start": "",
	"curfew_end": "",
	"curfew_duration": [],
	"trail_monitoring": "",
	"exclusion_zones": "",
	"exclusion_zones_duration": "",
	"inclusion_zones": "",
	"inclusion_zones_duration": "",
	"abstinence": "true",
	"schedule": "",
	"checkin_schedule": "",
	"revocation_date": "",
	"revocation_type": "",
	"order_status": "Not Started"
}
    """.trimIndent()
    assertThat(savedResult.fmsDeviceWearerRequest).isEqualTo(mockDeviceWearerJson.removeWhitespaceAndNewlines())
    assertThat(savedResult.fmsOrderRequest).isEqualTo(mockOrderJson.removeWhitespaceAndNewlines())
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
