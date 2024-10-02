package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement

import java.util.*

data class DocumentUploadResponse(
  val documentUuid: UUID? = null,

  val documentFilename: String? = null,

  val filename: String? = null,

  val fileExtension: String? = null,

  val mimeType: String? = null,
)
