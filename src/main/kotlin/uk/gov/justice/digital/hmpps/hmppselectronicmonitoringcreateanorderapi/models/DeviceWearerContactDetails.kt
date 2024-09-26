package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidPhoneNumber
import java.util.*

@Entity
@Table(name = "DEVICE_WEARER_CONTACT_DETAILS")
data class DeviceWearerContactDetails(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "CONTACT_NUMBER", nullable = true)
  @field:ValidPhoneNumber
  var contactNumber: String? = null,

  @OneToOne
  private val order: OrderForm,
)
