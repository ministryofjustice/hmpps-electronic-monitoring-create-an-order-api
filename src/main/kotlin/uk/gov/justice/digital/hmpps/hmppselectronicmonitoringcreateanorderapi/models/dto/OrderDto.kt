package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MandatoryAttendanceConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.util.*

data class OrderDto(
  val id: UUID,

  val additionalDocuments: MutableList<AdditionalDocument>,

  val addresses: MutableList<Address>,

  val contactDetails: ContactDetails?,

  val curfewConditions: CurfewConditions?,

  val curfewReleaseDateConditions: CurfewReleaseDateConditions?,

  val curfewTimeTable: MutableList<CurfewTimeTable>,

  val deviceWearer: DeviceWearer?,

  val deviceWearerResponsibleAdult: ResponsibleAdult?,

  val enforcementZoneConditions: MutableList<EnforcementZoneConditions>?,

  val fmsResultId: UUID?,

  val installationAndRisk: InstallationAndRisk?,

  val interestedParties: InterestedParties?,

  val isValid: Boolean,

  val mandatoryAttendanceConditions: MutableList<MandatoryAttendanceConditions>,

  val monitoringConditions: MonitoringConditions?,

  val monitoringConditionsAlcohol: AlcoholMonitoringConditions?,

  val monitoringConditionsTrail: TrailMonitoringConditions?,

  val status: OrderStatus,

  val type: RequestType,

  val username: String,

  val variationDetails: VariationDetails?,
)
