package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import tools.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderService
import java.time.ZonedDateTime

class ReturnsEventTranslatorTest {

  private val rejectOrder = mock<RejectOrderService>()
  private val translator = ReturnsEventTranslator(rejectOrder, jacksonObjectMapper())

  @Test
  fun `translates a rejected returns event into a reject order command`() {
    translator.processEvent(
      returnsEvent(
        caseId = "CASE123",
        reasons = """[{"section": "Section A", "details": "A details"}]""",
      ),
    )

    verify(rejectOrder).execute(
      eq("CASE123"),
      any(),
      eq(listOf(RejectionReason(section = "Section A", details = "A details"))),
    )
  }

  @Test
  fun `converts the time of the status change to Europe London`() {
    translator.processEvent(returnsEvent(datetimeOfStatusChange = "2026-09-23T10:15:00Z"))

    val dateTime = argumentCaptor<ZonedDateTime>()
    verify(rejectOrder).execute(any(), dateTime.capture(), any())

    assertThat(dateTime.firstValue).isEqualTo(ZonedDateTime.parse("2026-09-23T11:15:00+01:00[Europe/London]"))
  }

  @Test
  fun `translates a rejected returns event carrying no reasons`() {
    translator.processEvent(returnsEvent(reasons = "[]"))

    verify(rejectOrder).execute(any(), any(), eq(emptyList<RejectionReason>()))
  }

  @Test
  fun `rejects a malformed returns event so that it is dead lettered`() {
    assertThatThrownBy { translator.processEvent("BAD JSON") }.isInstanceOf(Exception::class.java)

    verifyNoInteractions(rejectOrder)
  }

  @Test
  fun `rejects a returns event with an unrecognised status`() {
    assertThatThrownBy { translator.processEvent(returnsEvent(status = "approved")) }
      .isInstanceOf(Exception::class.java)

    verifyNoInteractions(rejectOrder)
  }

  @Test
  fun `rejects a returns event with an unparseable time of status change`() {
    assertThatThrownBy { translator.processEvent(returnsEvent(datetimeOfStatusChange = "not a date")) }
      .isInstanceOf(Exception::class.java)

    verifyNoInteractions(rejectOrder)
  }

  private fun returnsEvent(
    caseId: String = "CASE123",
    status: String = "rejected",
    datetimeOfStatusChange: String = "2026-09-23T10:15:00Z",
    reasons: String = """[{"section": "Section A", "details": "A details"}]""",
  ): String {
    val message = """
      {
        "caseId": "$caseId",
        "status": "$status",
        "reasons": $reasons,
        "datetimeOfStatusChange": "$datetimeOfStatusChange"
      }
    """.trimIndent()

    return snsEnvelope(message)
  }

  private fun snsEnvelope(message: String) = """
    {
      "Type": "Notification",
      "MessageId": "eed5fdf9-ea08-5bf2-9d96-a27aed48bb71",
      "TopicArn": "arn:aws:sns:eu-west-2:000000000000:returns_events_topic",
      "Message": ${jacksonObjectMapper().writeValueAsString(message)},
      "Timestamp": "2026-09-23T10:15:00.000Z"
    }
  """
}
