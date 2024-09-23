package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "DEVICE_WEARER")
data class DeviceWearer(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false, unique = true)
  val orderId: UUID,

  @Column(name = "FIRST_NAME", nullable = true)
  var firstName: String? = null,

  @Column(name = "LAST_NAME", nullable = true)
  var lastName: String? = null,

  @Column(name = "GENDER", nullable = true)
  var gender: String? = null,

  @Column(name = "DATE_OF_BIRTH", nullable = true)
  var dateOfBirth: LocalDate? = null,
)
