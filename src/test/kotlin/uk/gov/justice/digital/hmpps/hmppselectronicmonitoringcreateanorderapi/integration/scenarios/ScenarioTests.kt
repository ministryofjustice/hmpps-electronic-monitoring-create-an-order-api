package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.scenarios

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities.OrderBuilder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SentenceType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ScenarioTests : IntegrationTestBase() {
  @Autowired
  lateinit var fmsResultRepository: FmsSubmissionResultRepository

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val mockStartDateInBritishTime = mockStartDate.toInstant().atZone(
    ZoneId.of("Europe/London"),
  ).format(dateTimeFormatter)
  val mockEndDateInBritishTime = mockEndDate.toInstant().atZone(ZoneId.of("Europe/London")).format(dateTimeFormatter)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    fmsResultRepository.deleteAll()

    sercoAuthApi.stubGrantToken()

    sercoApi.stubCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )

    sercoApi.stubCreateMonitoringOrder(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
    )
  }

  fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

  fun createReadyToSubmitOrder(block: OrderBuilder.() -> Unit): Order {
    val builder = OrderBuilder()
    builder.block()
    return builder.build()
  }

  @Nested
  @DisplayName("POST /api/orders/{orderId}/submit")
  inner class SubmitOrder {
    @Test
    fun cemo001() {
      val orderId = UUID.randomUUID()
      val versionId = UUID.randomUUID()

      val order = createReadyToSubmitOrder {
        this.orderId = orderId
        this.initialVersionId = versionId
        this.noFixedAddress = false
        this.initialRequestType = RequestType.REQUEST
        this.orderStatus = OrderStatus.IN_PROGRESS
        this.documents = mutableListOf(
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            versionId = versionId,
            fileType = DocumentType.LICENCE,
            fileName = "mockFile",
          ),
        )

        deviceWearer {
          interpreterRequired = true
          language = "French"
          noFixedAbode = false
        }
        addAddress {
          addressLine1 = "10 downing street"
          addressLine2 = "London"
          postcode = "SW1A 2AA"
          addressType = AddressType.PRIMARY
        }
        installationAndRisk {}
        contactDetails {}
        monitoringConditions {
          startDate = mockStartDate
          endDate = mockEndDate
          orderType = OrderType.PRE_TRIAL
          conditionType = MonitoringConditionType.LICENSE_CONDITION_OF_A_CUSTODIAL_ORDER
          sentenceType = SentenceType.LIFE_SENTENCE
          curfew = true
        }
        curfewConditions {
          startDate = mockStartDate
          endDate = mockEndDate
        }
        curfewReleaseDateConditions {
          releaseDate = mockStartDate
        }
        interestedParties {
          notifyingOrganisation = "Crown Court"
          responsibleOrganisation = "Police"
          notifyingOrganisationName = "Bolton Crown Court"
        }
        probationDeliveryUnit {}
      }
      repo.save(order)

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      documentApi.stubGetDocument(order.additionalDocuments.first().id.toString())
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())

      webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      val expectedDWJson = """
      {
        "title": "",
        "first_name": "John",
        "middle_name": "",
        "last_name": "Smith",
        "alias": "Johnny",
        "date_of_birth": "1990-01-01",
        "adult_child": "adult",
        "sex": "Male",
        "gender_identity": "Male",
        "disability": [
          {
            "disability": "Vision"
          },
          {
            "disability": "Learning, understanding or concentrating"
          }
        ],
        "address_1": "10 downing street",
        "address_2": "London",
        "address_3": "",
        "address_4": "N/A",
        "address_post_code": "SW1A 2AA",
        "secondary_address_1": "",
        "secondary_address_2": "",
        "secondary_address_3": "",
        "secondary_address_4": "",
        "secondary_address_post_code": "",
        "phone_number": "00447401111111",
        "risk_serious_harm": "",
        "risk_self_harm": "",
        "risk_details": "Danger",
        "mappa": "MAPPA 1",
        "mappa_case_type": "CPPC (Critical Public Protection Case)",
        "risk_categories": [
          {
            "category": "Sexual Offences"
          },
          {
            "category": "Risk to Specific Gender"
          }
        ],
        "responsible_adult_required": "false",
        "parent": "",
        "guardian": "",
        "parent_address_1": "",
        "parent_address_2": "",
        "parent_address_3": "",
        "parent_address_4": "",
        "parent_address_post_code": "",
        "parent_phone_number": null,
        "parent_dob": "",
        "pnc_id": "pncId",
        "nomis_id": "nomisId",
        "delius_id": "deliusId",
        "prison_number": "prisonNumber",
        "home_office_case_reference_number": "homeOfficeReferenceNumber",
        "interpreter_required": "true",
        "language": "French"
      }
      """.trimIndent()

      val expectedOrderJson = """
      {
        "case_id": "MockDeviceWearerId",
        "allday_lockdown": "",
        "atv_allowance": "",
        "condition_type": "License Condition of a Custodial Order",
        "court": "",
        "court_order_email": "",
        "device_type": "",
        "device_wearer": "John Smith",
        "enforceable_condition": [
          {
      			"condition": "Curfew with EM",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
          }
        ],
        "exclusion_allday": "",
        "interim_court_date": "",
        "issuing_organisation": "",
        "media_interest": "",
        "new_order_received": "",
        "notifying_officer_email": "",
        "notifying_officer_name": "",
        "notifying_organization": "Crown Court",
        "no_post_code": "",
        "no_address_1": "",
        "no_address_2": "",
        "no_address_3": "",
        "no_address_4": "",
        "no_email": "",
        "no_name": "Bolton Crown Court",
        "no_phone_number": "",
        "offence": "Fraud Offences",
        "offence_date": "",
      	"order_end": "$mockEndDateInBritishTime",
        "order_id": "$orderId",
        "order_request_type": "New Order",
      	"order_start": "$mockStartDateInBritishTime",
        "order_type": "Pre-Trial",
        "order_type_description": "DAPOL",
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
        "responsible_officer_phone": "00447401111111",
        "responsible_officer_name": "John Smith",
        "responsible_organization": "Police",
        "ro_post_code": "",
        "ro_address_1": "",
        "ro_address_2": "",
        "ro_address_3": "",
        "ro_address_4": "",
        "ro_email": "abc@def.com",
        "ro_phone": "",
        "ro_region": "London",
        "sentence_date": "",
        "sentence_expiry": "",
        "sentence_type": "Life Sentence",
        "tag_at_source": "",
        "tag_at_source_details": "",
        "technical_bail": "",
        "trial_date": "",
        "trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
        "reason_for_order_ending_early": "",
        "business_unit": "",
        "service_end_date": "${mockEndDate.format(formatter)}",
        "curfew_description": "",
      	"curfew_start": "$mockStartDateInBritishTime",
      	"curfew_end": "$mockEndDateInBritishTime",
        "curfew_duration": [
          {
            "location": "primary",
            "allday": "",
            "schedule": [
              {
                "day": "Mo",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Tu",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Wed",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Th",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Fr",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Sa",
                "start": "17:00",
                "end": "09:00"
              },
              {
                "day": "Su",
                "start": "17:00",
                "end": "09:00"
              }
            ]
          }
        ],
        "trail_monitoring": "",
        "exclusion_zones": [],
        "inclusion_zones": [],
        "abstinence": "",
        "schedule": "",
        "checkin_schedule": [],
        "revocation_date": "",
        "revocation_type": "",
        "installation_address_1": "10 downing street",
        "installation_address_2": "London",
        "installation_address_3": "",
        "installation_address_4": "",
        "installation_address_post_code": "SW1A 2AA",
        "crown_court_case_reference_number": "",
        "magistrate_court_case_reference_number": "",
        "issp": "Yes",
        "hdc": "No",
        "order_status": "Not Started"
      }
      """.trimIndent()

      assertThat(submitResult!!.deviceWearerResult.payload).isEqualTo(expectedDWJson.removeWhitespaceAndNewlines())
      assertThat(submitResult.monitoringOrderResult.payload).isEqualTo(expectedOrderJson.removeWhitespaceAndNewlines())
      assertThat(submitResult.attachmentResults[0].sysId).isEqualTo("MockSysId")
      assertThat(
        submitResult.attachmentResults[0].fileType,
      ).isEqualTo(order.additionalDocuments.first().fileType.toString())
      assertThat(
        submitResult.attachmentResults[0].attachmentId,
      ).isEqualTo(order.additionalDocuments.first().id.toString())
      val updatedOrder = getOrder(order.id)
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)
    }
  }
}
