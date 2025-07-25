package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.validators

import jakarta.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType

object FileUploadValidator {
  fun validateFileExtensionAndSize(multipartFile: MultipartFile, documentType: DocumentType) {
    val config = documentType.config
    val maxSizeInBytes = config.maxSizeInMB * 1024 * 1024
    val extension = FilenameUtils.getExtension(multipartFile.originalFilename)?.lowercase()

    if (!StringUtils.hasLength(extension) || !config.allowedExtensions.contains(extension)
    ) {
      throw ValidationException(config.invalidExtensionMessage)
    }

    if (multipartFile.size > maxSizeInBytes) {
      throw MaxUploadSizeExceededException(config.maxSizeInMB)
    }
  }
}
