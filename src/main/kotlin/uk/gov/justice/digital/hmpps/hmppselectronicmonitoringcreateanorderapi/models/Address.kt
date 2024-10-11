package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "ADDRESS")
data class Address(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ADDRESS_LINE_1", nullable = true)
  var addressLine1: String = "",

  @Column(name = "ADDRESS_LINE_2", nullable = true)
  var addressLine2: String = "",

  @Column(name = "ADDRESS_LINE_3", nullable = true)
  var addressLine3: String = "",

  @Column(name = "ADDRESS_LINE_4", nullable = true)
  var addressLine4: String = "",

  @Column(name = "POSTCODE", nullable = true)
  var postcode: String = "",
)
