package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.util.*

@Entity
@Table(name = "ADDRESS")
data class Address(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ADDRESS_LINE_1", nullable = false)
  @field:NotBlank(message = "Address line 1 is required")
  var addressLine1: String,

  @Column(name = "ADDRESS_LINE_2", nullable = false)
  @field:NotBlank(message = "Address line 2 is required")
  var addressLine2: String,

  @Column(name = "ADDRESS_LINE_3", nullable = false)
  var addressLine3: String = "",

  @Column(name = "ADDRESS_LINE_4", nullable = false)
  var addressLine4: String = "",

  @Column(name = "POSTCODE", nullable = false)
  @field:NotBlank(message = "Postcode is required")
  var postcode: String,
)
