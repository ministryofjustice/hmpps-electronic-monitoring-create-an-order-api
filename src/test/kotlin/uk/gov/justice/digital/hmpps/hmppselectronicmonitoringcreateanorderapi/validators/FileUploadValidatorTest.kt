package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.validators

import jakarta.validation.ValidationException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import java.io.ByteArrayInputStream

class FileUploadValidatorTest {

  private lateinit var multipartFile: MultipartFile
  private val documentType: DocumentType = DocumentType.PHOTO_ID

  @BeforeEach
  fun setup() {
    multipartFile = mock()
  }

  @Test
  fun `should throw when extension is missing`() {
    whenever(multipartFile.originalFilename).thenReturn("file")
    whenever(multipartFile.size).thenReturn(100L)

    val ex = assertThrows(ValidationException::class.java) {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType)
    }

    assertEquals("Select a PDF, PNG, JPEG or JPG", ex.message)
  }

  @Test
  fun `should throw when extension is not allowed`() {
    whenever(multipartFile.originalFilename).thenReturn("file.exe")
    whenever(multipartFile.size).thenReturn(100L)

    val ex = assertThrows(ValidationException::class.java) {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType)
    }

    assertEquals("Select a PDF, PNG, JPEG or JPG", ex.message)
  }

  @Test
  fun `should throw when file size exceeds limit`() {
    whenever(multipartFile.originalFilename).thenReturn("file.pdf")
    whenever(multipartFile.size).thenReturn(11 * 1024 * 1024L) // 10MB
    whenever(multipartFile.inputStream).thenReturn(ByteArrayInputStream(ByteArray(10)))

    val tikaMock = mock<Tika>()
    whenever(tikaMock.detect(org.mockito.kotlin.any<java.io.InputStream>()))
      .thenReturn("application/pdf")

    val ex = assertThrows(MaxUploadSizeExceededException::class.java) {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType, tikaMock)
    }
  }

  @Test
  fun `should throw when mime type does not match extension`() {
    whenever(multipartFile.originalFilename).thenReturn("file.pdf")
    whenever(multipartFile.size).thenReturn(100L)
    whenever(multipartFile.inputStream).thenReturn(ByteArrayInputStream(ByteArray(10)))

    val tikaMock = mock<Tika>()
    whenever(tikaMock.detect(org.mockito.kotlin.any<java.io.InputStream>()))
      .thenReturn("image/png")

    val ex = assertThrows(ValidationException::class.java) {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType, tikaMock)
    }

    assertEquals("The selected file could not be uploaded - there is an issue with the file", ex.message)
  }

  @Test
  fun `should throw when mime detection fails`() {
    whenever(multipartFile.originalFilename).thenReturn("file.pdf")
    whenever(multipartFile.size).thenReturn(100L)
    whenever(multipartFile.inputStream).thenThrow(RuntimeException("read error"))

    val ex = assertThrows(ValidationException::class.java) {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType)
    }

    assertEquals("The selected file could not be uploaded - there is an issue with the file", ex.message)
  }

  @Test
  fun `should pass validation for valid pdf file`() {
    whenever(multipartFile.originalFilename).thenReturn("file.pdf")
    whenever(multipartFile.size).thenReturn(100L)

    val inputStream = ByteArrayInputStream(ByteArray(10))
    whenever(multipartFile.inputStream).thenReturn(inputStream)

    val tikaMock = mock<Tika>()
    whenever(tikaMock.detect(org.mockito.kotlin.any<java.io.InputStream>()))
      .thenReturn("application/pdf")

    assertDoesNotThrow {
      FileUploadValidator.validateFileExtensionAndSize(multipartFile, documentType, tikaMock)
    }
  }
}
