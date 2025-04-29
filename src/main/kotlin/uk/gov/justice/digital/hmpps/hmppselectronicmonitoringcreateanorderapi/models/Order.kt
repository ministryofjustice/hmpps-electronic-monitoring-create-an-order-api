package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "ORDERS")
data class Order(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [ALL], mappedBy = "order", orphanRemoval = true)
  var versions: MutableList<OrderVersion> = mutableListOf(),

) {
  fun getCurrentVersion(): OrderVersion = versions.maxBy { it.versionId }

  fun createVariation(username: String): OrderVersion {
    if (getCurrentVersion().status === OrderStatus.IN_PROGRESS) {
      throw Exception("Order is already in progress")
    }

    val versionId = versions.size + 1
    val version = OrderVersion(
      orderId = id,
      status = OrderStatus.IN_PROGRESS,
      type = RequestType.VARIATION,
      username = username,
      versionId = versionId,
    )

    versions.add(version)
    return version
  }

  fun deleteCurrentVersion() {
    val version = getCurrentVersion()

    if (version.status != OrderStatus.IN_PROGRESS) {
      throw IllegalStateException("Order with id $id cannot be deleted because it has already been submitted")
    }

    versions.remove(version)
  }

  val additionalDocuments: MutableList<AdditionalDocument>
    get() {
      return getCurrentVersion().additionalDocuments
    }

  val addresses: MutableList<Address>
    get() {
      return getCurrentVersion().addresses
    }

  var contactDetails: ContactDetails?
    get() {
      return getCurrentVersion().contactDetails
    }
    set(contactDetails) {
      getCurrentVersion().contactDetails = contactDetails
    }

  var curfewConditions: CurfewConditions?
    get() {
      return getCurrentVersion().curfewConditions
    }
    set(curfewConditions) {
      getCurrentVersion().curfewConditions = curfewConditions
    }

  var curfewReleaseDateConditions: CurfewReleaseDateConditions?
    get() {
      return getCurrentVersion().curfewReleaseDateConditions
    }
    set(curfewReleaseDateConditions) {
      getCurrentVersion().curfewReleaseDateConditions = curfewReleaseDateConditions
    }

  val curfewTimeTable: MutableList<CurfewTimeTable>
    get() {
      return getCurrentVersion().curfewTimeTable
    }

  var deviceWearer: DeviceWearer?
    get() {
      return getCurrentVersion().deviceWearer
    }
    set(deviceWearer) {
      getCurrentVersion().deviceWearer = deviceWearer
    }

  var deviceWearerResponsibleAdult: ResponsibleAdult?
    get() {
      return getCurrentVersion().deviceWearerResponsibleAdult
    }
    set(responsibleAdult) {
      getCurrentVersion().deviceWearerResponsibleAdult = responsibleAdult
    }

  val enforcementZoneConditions: MutableList<EnforcementZoneConditions>
    get() {
      return getCurrentVersion().enforcementZoneConditions
    }

  var fmsResultId: UUID?
    get() {
      return getCurrentVersion().fmsResultId
    }
    set(fmsResultId) {
      getCurrentVersion().fmsResultId = fmsResultId
    }

  var fmsResultDate: OffsetDateTime?
    get() {
      return getCurrentVersion().fmsResultDate
    }
    set(fmsResultDate) {
      getCurrentVersion().fmsResultDate = fmsResultDate
    }

  var installationAndRisk: InstallationAndRisk?
    get() {
      return getCurrentVersion().installationAndRisk
    }
    set(installationAndRisk) {
      getCurrentVersion().installationAndRisk = installationAndRisk
    }

  var interestedParties: InterestedParties?
    get() {
      return getCurrentVersion().interestedParties
    }
    set(interestedParties) {
      getCurrentVersion().interestedParties = interestedParties
    }

  val isValid: Boolean
    get() {
      return getCurrentVersion().isValid
    }

  val mandatoryAttendanceConditions: MutableList<MandatoryAttendanceConditions>
    get() {
      return getCurrentVersion().mandatoryAttendanceConditions
    }

  var monitoringConditions: MonitoringConditions?
    get() {
      return getCurrentVersion().monitoringConditions
    }
    set(monitoringConditions) {
      getCurrentVersion().monitoringConditions = monitoringConditions
    }

  var monitoringConditionsAlcohol: AlcoholMonitoringConditions?
    get() {
      return getCurrentVersion().monitoringConditionsAlcohol
    }
    set(alcoholMonitoringConditions) {
      getCurrentVersion().monitoringConditionsAlcohol = alcoholMonitoringConditions
    }

  var monitoringConditionsTrail: TrailMonitoringConditions?
    get() {
      return getCurrentVersion().monitoringConditionsTrail
    }
    set(trailMonitoringConditions) {
      getCurrentVersion().monitoringConditionsTrail = trailMonitoringConditions
    }

  var status: OrderStatus
    get() {
      return getCurrentVersion().status
    }
    set(status) {
      getCurrentVersion().status = status
    }

  val type: RequestType
    get() {
      return getCurrentVersion().type
    }

  val username: String
    get() {
      return getCurrentVersion().username
    }

  var variationDetails: VariationDetails?
    get() {
      return getCurrentVersion().variationDetails
    }
    set(variationDetails) {
      getCurrentVersion().variationDetails = variationDetails
    }
}
