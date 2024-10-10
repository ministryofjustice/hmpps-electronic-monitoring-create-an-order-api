package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import java.util.*

@Repository
interface AdditionalDocumentRepository : JpaRepository<AdditionalDocument, UUID> {

  fun findAdditionalDocumentsByOrderIdAndOrderUsernameAndFileType(
    orderId: UUID,
    orderUserName: String,
    fileType: DocumentType,
  ): Optional<AdditionalDocument>
}
