package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import java.util.*

@Entity
@Table(name = "ADDITIONAL_DOCUMENTS")
data class AdditionalDocument(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false)
  val versionId: UUID,

  @Column(name = "FILE_NAME", nullable = true)
  var fileName: String? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "FILE_TYPE", nullable = true)
  var fileType: DocumentType,

  @Column(name = "DOCUMENT_ID", nullable = false)
  val documentId: UUID,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,

)
