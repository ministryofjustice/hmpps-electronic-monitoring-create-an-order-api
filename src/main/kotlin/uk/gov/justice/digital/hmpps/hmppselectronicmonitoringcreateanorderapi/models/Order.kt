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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.UUID

@Entity
@Table(name = "ORDERS")
data class Order(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "USER_NAME", nullable = false)
  var username: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  var status: OrderStatus,

  @Enumerated(EnumType.STRING)
  @Column(name = "TYPE", nullable = false)
  var type: RequestType,

  @Column(name = "FMS_RESULT_ID", nullable = true)
  var fmsResultId: UUID? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearer: DeviceWearer? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var deviceWearerResponsibleAdult: ResponsibleAdult? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var interestedParties: InterestedParties? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var contactDetails: ContactDetails? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var addresses: MutableList<Address> = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var monitoringConditions: MonitoringConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var monitoringConditionsTrail: TrailMonitoringConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var monitoringConditionsAlcohol: AlcoholMonitoringConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var installationAndRisk: InstallationAndRisk? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var additionalDocuments: MutableList<AdditionalDocument> = mutableListOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var enforcementZoneConditions: MutableList<EnforcementZoneConditions> = mutableListOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var mandatoryAttendanceConditions: MutableList<MandatoryAttendanceConditions>? = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewReleaseDateConditions: CurfewReleaseDateConditions? = null,

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewConditions: CurfewConditions? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var curfewTimeTable: MutableList<CurfewTimeTable> = mutableListOf(),

  @OneToOne(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var variationDetails: VariationDetails? = null,

) {
  private val adultOrHasResponsibleAdult: Boolean
    get() = (
      deviceWearer?.adultAtTimeOfInstallation == true ||
        (deviceWearer?.adultAtTimeOfInstallation == false && deviceWearerResponsibleAdult != null)
      )

  private val hasPrimaryAddressOrNoFixedAbode: Boolean
    get() = (
      (
        (deviceWearer?.noFixedAbode == false && addresses.any { it.addressType == AddressType.PRIMARY }) ||
          deviceWearer?.noFixedAbode == true
        )
      )

  private val monitoringConditionsAreValid: Boolean
    get() = (
      addresses.any { it.addressType == AddressType.INSTALLATION } &&
        (
          if (monitoringConditions?.curfew == true) {
            curfewReleaseDateConditions != null &&
              curfewConditions != null &&
              curfewTimeTable.isNotEmpty()
          } else {
            (true)
          }
          ) &&
        (
          if (monitoringConditions?.exclusionZone == true) {
            enforcementZoneConditions.isNotEmpty()
          } else {
            (true)
          }
          ) &&
        (
          if (monitoringConditions?.trail == true) {
            monitoringConditionsTrail != null
          } else {
            (true)
          }
          ) &&
        (
          if (monitoringConditions?.mandatoryAttendance == true) {
            // Mandatory attendance conditions aren't currently persisted. When they are, validate them here. eg:
            // mandatoryAttendanceConditions != null
            true
          } else {
            (true)
          }
          ) &&
        (
          if (monitoringConditions?.alcohol == true) {
            monitoringConditionsAlcohol != null
          } else {
            (true)
          }
          )
      )

  private val isOrderOrHasVariationDetails: Boolean
    get() = (
      type === RequestType.REQUEST || variationDetails != null
      )

  private val requiredDocuments: Boolean
    get() = (
//      Add additional document validation here
//      eg. if license is a mandatory attachment:
//      additionalDocuments.any { it.fileType == DocumentType.LICENCE }
      true
      )

  val isValid: Boolean
    get() = (
      deviceWearer?.isValid == true &&
        monitoringConditions?.isValid == true &&
        adultOrHasResponsibleAdult &&
        hasPrimaryAddressOrNoFixedAbode &&
        monitoringConditionsAreValid &&
        isOrderOrHasVariationDetails
      )
}
