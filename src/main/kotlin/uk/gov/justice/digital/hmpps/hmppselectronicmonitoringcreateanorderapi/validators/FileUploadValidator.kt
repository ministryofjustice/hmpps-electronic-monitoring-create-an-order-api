package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.validators

import jakarta.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.apache.tika.Tika
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType

object FileUploadValidator {
  private val tika = Tika()
  fun validateFileExtensionAndSize(multipartFile: MultipartFile, documentType: DocumentType, tika: Tika = this.tika) {
    val config = documentType.config
    val maxSizeInBytes = config.maxSizeInMB * 1024 * 1024
    val extension = FilenameUtils.getExtension(multipartFile.originalFilename)?.lowercase()

    if (!StringUtils.hasLength(extension) || !config.allowedExtensions.contains(extension)
    ) {
      throw ValidationException(config.invalidExtensionMessage)
    }

    var detectedMimeType: String? = null
    try {
      detectedMimeType = multipartFile.inputStream.use { tika.detect(it) }
    } catch (ex: Exception) {
      throw ValidationException(ValidationErrors.AdditionalDocuments.INVALID_FILE)
    }

    if (!matchesMimeType(extension!!, detectedMimeType)) {
      throw ValidationException(ValidationErrors.AdditionalDocuments.INVALID_FILE)
    }

    if (multipartFile.size > maxSizeInBytes) {
      throw MaxUploadSizeExceededException(config.maxSizeInMB)
    }
  }

  private fun matchesMimeType(extension: String, mimeType: String): Boolean = when (extension) {
    "pdf" -> mimeType == "application/pdf"

    "png" -> mimeType == "image/png"

    "jpg", "jpeg" -> mimeType == "image/jpeg"

    "doc" -> mimeType == "application/msword"

    "docx" -> mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    else -> false
  }
}
