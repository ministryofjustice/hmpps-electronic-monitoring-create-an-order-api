package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.UUID

data class OrderSearchResultDeviceWearerDto(
  val firstName: String? = null,
  val lastName: String? = null,
  val dateOfBirth: ZonedDateTime? = null,
  val adultAtTimeOfInstallation: Boolean? = null,
  val nomisId: String? = null,
  val pncId: String? = null,
  val deliusId: String? = null,
  val prisonNumber: String? = null,
  val homeOfficeReferenceNumber: String? = null,
  val complianceAndEnforcementPersonReference: String? = null,
  val courtCaseReferenceNumber: String? = null,
)

data class OrderSearchResultAddressDto(val addressType: AddressType, val addressLine3: String)

data class OrderSearchResultMonitoringConditionsDto(
  val startDate: ZonedDateTime? = null,
  val endDate: ZonedDateTime? = null,
)

// Slimmer representation of Order
data class OrderSearchResultDto(
  val id: UUID,
  val status: OrderStatus,
  val type: RequestType,
  val fmsResultDate: OffsetDateTime? = null,
  val deviceWearer: OrderSearchResultDeviceWearerDto,
  val addresses: List<OrderSearchResultAddressDto>,
  val monitoringConditions: OrderSearchResultMonitoringConditionsDto,
)
