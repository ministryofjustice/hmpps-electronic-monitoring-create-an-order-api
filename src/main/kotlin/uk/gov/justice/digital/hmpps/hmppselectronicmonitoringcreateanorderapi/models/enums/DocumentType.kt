package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

data class FileUploadConfig(val maxSizeInMB: Long, val allowedExtensions: List<String>)

enum class DocumentType(val config: FileUploadConfig) {
  LICENCE(
    FileUploadConfig(
      maxSizeInMB = 25,
      allowedExtensions = listOf("pdf", "doc", "docx"),
    ),
  ),
  PHOTO_ID(
    FileUploadConfig(
      maxSizeInMB = 10,
      allowedExtensions = listOf("pdf", "png", "jpeg", "jpg"),
    ),
  ),
  ENFORCEMENT_ZONE_MAP(
    FileUploadConfig(
      maxSizeInMB = 10,
      allowedExtensions = listOf("pdf", "jpeg", "jpg"),
    ),
  ),
}
