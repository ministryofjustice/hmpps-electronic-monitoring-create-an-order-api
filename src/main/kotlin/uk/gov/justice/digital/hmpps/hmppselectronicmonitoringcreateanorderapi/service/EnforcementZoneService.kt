package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client.DocumentApiClient
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement.DocumentMetadata
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.EnforcementZoneRepository
import java.util.*

@Service
class EnforcementZoneService(
  val repo: EnforcementZoneRepository,
  val webClient: DocumentApiClient,

) : OrderSectionServiceBase() {

  val allowedFileExtensions: List<String> = listOf("pdf", "jpeg", "jpg")

  fun updateEnforcementZone(orderId: UUID, username: String, enforcementZone: EnforcementZoneConditions) {
    val order = findEditableOrder(orderId, username)
    // remove existing enforcement zone
    order.enforcementZoneConditions.firstOrNull { it.zoneId == enforcementZone.zoneId }?.let { zone ->
      order.enforcementZoneConditions.remove(zone)
      repo.deleteById(zone.id)
      if (zone.fileId != null) {
        webClient.deleteDocument(zone.fileId.toString())
      }
    }

    repo.save(enforcementZone)
  }

  fun uploadEnforcementZoneAttachment(orderId: UUID, username: String, zoneId: Int, multipartFile: MultipartFile) {
    validateFileExtension(multipartFile)
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
    repo.save(zone)
  }

  private fun validateFileExtension(multipartFile: MultipartFile) {
    val extension = FilenameUtils.getExtension(multipartFile.originalFilename)?.lowercase()
    if (!StringUtils.hasLength(extension) || !allowedFileExtensions.contains(extension)
    ) {
      throw ValidationException(
        String.format(
          "Unsupported or missing file type %s. Supported file types: %s",
          extension,
          allowedFileExtensions.joinToString(),
        ),
      )
    }
  }
}
