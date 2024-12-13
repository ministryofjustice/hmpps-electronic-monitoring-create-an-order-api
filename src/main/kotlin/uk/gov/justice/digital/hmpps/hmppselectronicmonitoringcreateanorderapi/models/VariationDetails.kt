package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "VARIATION_DETAILS")
data class VariationDetails(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Enumerated(EnumType.STRING)
  @Column(name = "VARIATION_TYPE", nullable = false)
  val variationType: VariationType,

  @Column(name = "VARIATION_DATE", nullable = true)
  var variationDate: ZonedDateTime? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
