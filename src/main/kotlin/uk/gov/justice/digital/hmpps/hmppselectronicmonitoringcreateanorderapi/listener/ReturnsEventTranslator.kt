package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderService
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReturnsSnsEnvelope(@JsonProperty("Message") val message: String)

@Component
class ReturnsEventTranslator(private val rejectOrder: RejectOrderService, private val objectMapper: ObjectMapper) {
  @SqsListener("returnseventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun processEvent(rawMessage: String) {
    try {
      val envelope: ReturnsSnsEnvelope = objectMapper.readValue(rawMessage)
      val message: ReturnMessage = objectMapper.readValue(envelope.message)
      val dateTime = ZonedDateTime.ofInstant(Instant.parse(message.datetimeOfStatusChange), ZoneId.of("Europe/London"))

      when (message.status) {
        ReturnStatus.REJECTED ->
          rejectOrder.execute(
            message.caseId,
            dateTime,
            message.reasons.map { RejectionReason(section = it.section, details = it.details) },
          )
      }
    } catch (e: Exception) {
      log.error("Failed to process returns event: ${e.message}")
      throw e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
