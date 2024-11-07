package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber
import java.util.*

@Entity
@Table(name = "DEVICE_WEARER_CONTACT_DETAILS")
data class ContactDetails(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "CONTACT_NUMBER", nullable = true)
  @field:ValidPhoneNumber
  var contactNumber: String? = null,

  @OneToOne
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
) {
  val isValid: Boolean
    get() = (
      contactNumber != null
      )
}
