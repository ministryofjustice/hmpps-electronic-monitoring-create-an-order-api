package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.documentmanagement

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import java.util.UUID

data class DocumentMetadata(
  val orderId: UUID?,
  val documentType: DocumentType?,
)
