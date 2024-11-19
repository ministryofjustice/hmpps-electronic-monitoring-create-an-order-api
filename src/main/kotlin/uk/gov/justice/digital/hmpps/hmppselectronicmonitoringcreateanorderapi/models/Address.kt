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
import jakarta.persistence.UniqueConstraint
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import java.util.*

@Entity
@Table(
  name = "ADDRESS",
  uniqueConstraints = [
    UniqueConstraint(columnNames = ["ORDER_ID", "ADDRESS_TYPE"]),
  ],
)
data class Address(
  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "ORDER_ID", nullable = false)
  val orderId: UUID,

  @Enumerated(EnumType.STRING)
  @Column(name = "ADDRESS_TYPE", nullable = false)
  var addressType: AddressType,

  @Enumerated(EnumType.STRING)
  @Column(name = "ADDRESS_USAGE", nullable = false)
  var addressUsage: DeviceWearerAddressUsage = DeviceWearerAddressUsage.NA,

  @Column(name = "ADDRESS_LINE_1", nullable = false)
  var addressLine1: String,

  @Column(name = "ADDRESS_LINE_2", nullable = false)
  var addressLine2: String,

  @Column(name = "ADDRESS_LINE_3", nullable = false)
  var addressLine3: String = "",

  @Column(name = "ADDRESS_LINE_4", nullable = false)
  var addressLine4: String = "",

  @Column(name = "POSTCODE", nullable = false)
  var postcode: String,

  @Schema(hidden = true)
  @ManyToOne(optional = true)
  @JoinColumn(name = "ORDER_ID", updatable = false, insertable = false)
  private val order: Order? = null,
)
