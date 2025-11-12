package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentMetadata
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateEnforcementZoneDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.validators.FileUploadValidator.validateFileExtensionAndSize
import java.util.*

@Service
class EnforcementZoneService(val webClient: DocumentApiClient) : OrderSectionServiceBase() {

  fun updateEnforcementZone(orderId: UUID, username: String, updateRecord: UpdateEnforcementZoneDto) {
    val order = findEditableOrder(orderId, username)
    val zone = order.enforcementZoneConditions.firstOrNull { it.zoneId == updateRecord.zoneId }

    // Remove existing enforcement zone
    if (zone != null) {
      order.enforcementZoneConditions.remove(zone)

      if (zone.fileId != null) {
        webClient.deleteDocument(zone.fileId.toString())
      }
    }

    // Add new enforcement zone
    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        versionId = order.getCurrentVersion().id,
        name = updateRecord.name,
        description = updateRecord.description,
        duration = updateRecord.duration,
        endDate = updateRecord.endDate,
        startDate = updateRecord.startDate,
        zoneId = updateRecord.zoneId,
        zoneType = updateRecord.zoneType,
      ),
    )

    orderRepo.save(order)
  }

  fun uploadEnforcementZoneAttachment(orderId: UUID, username: String, zoneId: Int, multipartFile: MultipartFile) {
    validateFileExtensionAndSize(multipartFile, DocumentType.ENFORCEMENT_ZONE_MAP)

    val order = findEditableOrder(orderId, username)
    val zone = order.enforcementZoneConditions.firstOrNull { it.zoneId == zoneId }
    if (zone == null) {
      throw EntityNotFoundException(
        "Enforcement zone with  $zoneId does not exist in order with id $orderId",
      )
    }
    // clear previous file in document api
    zone.fileId?.let { fileId -> webClient.deleteDocument(fileId.toString()) }
    // upload new file and save enforcement zone to database
    val fileId = UUID.randomUUID()
    val builder = MultipartBodyBuilder()
    builder.part("file", multipartFile.resource)
      .contentType(MediaType.valueOf(multipartFile.contentType!!))
      .filename(multipartFile.originalFilename!!)
    builder.part("metadata", DocumentMetadata(orderId))
    webClient.createDocument(fileId.toString(), builder)

    zone.fileId = fileId
    zone.fileName = multipartFile.originalFilename

    order.enforcementZoneConditions.add(zone)
    orderRepo.save(order)
  }
}
