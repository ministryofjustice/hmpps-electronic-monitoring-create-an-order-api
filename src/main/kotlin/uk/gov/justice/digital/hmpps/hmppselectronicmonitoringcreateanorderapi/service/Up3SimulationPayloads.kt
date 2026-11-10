package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulateReturnDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnReasonDto
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Component
class Up3SimulationPayloads(private val mapper: ObjectMapper) {
  fun returnHash(orderId: UUID, request: SimulateReturnDto): String = returnHash(
    orderId,
    request.expectedCurrentOrderVersionId,
    request.expectedSimulatedSubmissionVersionId,
    request.datetimeOfStatusChange,
    request.reasons,
  )

  fun returnHash(
    orderId: UUID,
    currentVersionId: UUID,
    submissionVersionId: UUID,
    timestamp: Instant,
    reasons: List<SimulatedReturnReasonDto>,
  ): String = hash(
    mapper.writeValueAsString(
      listOf(
        orderId.toString(),
        currentVersionId.toString(),
        submissionVersionId.toString(),
        timestamp.toString(),
        reasons.map { listOf(it.section, it.details) },
      ),
    ),
  )

  fun creationHash(request: CreateSimulatedOrderDto): String {
    val order = mapper.valueToTree<ObjectNode>(request.order)
    order.remove(
      listOf(
        "id", "versionId", "fmsResultId", "fmsResultDate", "isValid", "status", "username",
        "submittedBy", "statusUpdates", "lastUpdatedBy", "lastUpdatedDateTime", "ownerCohort", "isOwner", "canAmend",
      ),
    )
    val payload = mapper.createObjectNode()
      .set("order", canonical(order, stripEntityIds = true))
    payload.put("state", request.state.name)
    payload.set("reasons", mapper.valueToTree(request.reasons))
    return hash(mapper.writeValueAsString(canonical(payload)))
  }

  private fun canonical(node: JsonNode, stripEntityIds: Boolean = false): JsonNode = when {
    node is ObjectNode -> mapper.createObjectNode().also { result ->
      node.properties().sortedBy { it.key }.forEach { (key, value) ->
        if (!stripEntityIds || key !in setOf("id", "versionId", "isValid")) {
          result.set(key, canonical(value, stripEntityIds))
        }
      }
    }
    node.isArray -> mapper.createArrayNode().also { result ->
      node.forEach { result.add(canonical(it, stripEntityIds)) }
    }
    else -> node
  }

  companion object {
    fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
      .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
  }
}
