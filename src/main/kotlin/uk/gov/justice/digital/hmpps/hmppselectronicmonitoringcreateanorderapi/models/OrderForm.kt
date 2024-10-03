package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import java.util.UUID

@Entity
@Table(name = "ORDER_FORM")
data class OrderForm(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "USER_NAME", nullable = false)
  var username: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  var status: FormStatus,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearer: DeviceWearer?= null ,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearerContactDetails: DeviceWearerContactDetails?= null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "orderForm", orphanRemoval = true)
  var monitoringConditions: MonitoringConditions?= null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var installationAndRisk: InstallationAndRisk?= null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var additionalDocuments: MutableList<AdditionalDocument> = mutableListOf(),

)

