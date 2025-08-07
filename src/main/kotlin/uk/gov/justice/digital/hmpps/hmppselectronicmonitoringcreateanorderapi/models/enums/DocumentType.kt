package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors

data class FileUploadConfig(
  val maxSizeInMB: Long,
  val allowedExtensions: List<String>,
  val invalidExtensionMessage: String,
)

enum class DocumentType(val config: FileUploadConfig) {
  LICENCE(
    FileUploadConfig(
      maxSizeInMB = 25,
      allowedExtensions = listOf("pdf", "doc", "docx"),
      invalidExtensionMessage = ValidationErrors.AdditionalDocuments.INVALID_LICENSE_FILE_EXTENSION,
    ),
  ),
  PHOTO_ID(
    FileUploadConfig(
      maxSizeInMB = 10,
      allowedExtensions = listOf("pdf", "png", "jpeg", "jpg"),
      invalidExtensionMessage = ValidationErrors.AdditionalDocuments.INVALID_PHOTO_ID_FILE_EXTENSION,
    ),
  ),
  ENFORCEMENT_ZONE_MAP(
    FileUploadConfig(
      maxSizeInMB = 10,
      allowedExtensions = listOf("pdf", "png", "jpeg", "jpg"),
      invalidExtensionMessage = ValidationErrors.EnforcementZone.INVALID_MAP_FILE_EXTENSION,
    ),
  ),
}
