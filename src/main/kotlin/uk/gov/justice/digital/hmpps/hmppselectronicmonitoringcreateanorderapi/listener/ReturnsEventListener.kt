package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

@Service
class ReturnsEventListener(
  private val objectMapper: ObjectMapper,
  private val submissionRepo: FmsSubmissionResultRepository,
  private val repo: OrderRepository,
) {
  @SqsListener("returnseventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    // deconstruct message
    val message: ReturnMessage = objectMapper.readValue(rawMessage)

    // get order version by case id
    val submissionResult =
      submissionRepo.findByCaseId(message.caseId)
        ?: throw Exception("Submission result does not exist for case id: ${message.caseId}")
    val order = repo.findById(submissionResult.orderId).get()

    // update order status to be failed
    order.status = OrderStatus.REJECTED
    repo.save(order)
  }
}
