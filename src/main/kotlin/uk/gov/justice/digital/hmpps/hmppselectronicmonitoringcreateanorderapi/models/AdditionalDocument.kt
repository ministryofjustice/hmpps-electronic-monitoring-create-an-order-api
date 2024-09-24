package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "ADDITIONAL_DOCUMENTIONS")
data class AdditionalDocument(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "FILE_NAME", nullable = true)
  var fileName: String? = null,

  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val orderForm: OrderForm? = null,

)