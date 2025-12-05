package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateProbationDeliveryUnitDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationDeliveryUnits
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.ProbationDeliveryUnitsDDv6
import java.util.*

@Service
class ProbationDeliveryUnitService : OrderSectionServiceBase() {
  fun updateProbationDeliveryUnit(
    orderId: UUID,
    username: String,
    updateProbationDeliveryUnitRecord: UpdateProbationDeliveryUnitDto,
  ): ProbationDeliveryUnit {
    // Verify the order belongs to the user and is in draft state
    val order = this.findEditableOrder(orderId, username)

    // Verify the responsible organisation is Probation
    val responsibleOrganisation = order.interestedParties?.responsibleOrganisation
    val responsibleOrganisationRegion = order.interestedParties?.responsibleOrganisationRegion
    if (responsibleOrganisation != ResponsibleOrganisation.PROBATION.name) {
      throw ValidationException(ValidationErrors.ProbationDeliveryUnit.RESPONSIBLE_ORGANISATION_NOT_PROBATION)
    }
    // Verify unit based on version
    val unitInput = updateProbationDeliveryUnitRecord.unit

    if (unitInput != null) {
      val isValid = if (order.dataDictionaryVersion == DataDictionaryVersion.DDV6) {
        val unitEnum = ProbationDeliveryUnitsDDv6.from(unitInput)
        unitEnum != null &&
          ProbationDeliveryUnitsDDv6.PROBATION_REGION_DELIVERY_UNIT[responsibleOrganisationRegion]?.contains(
            unitEnum,
          ) == true
      } else {
        val unitEnum = ProbationDeliveryUnits.from(unitInput)
        unitEnum != null &&
          ProbationDeliveryUnits.PROBATION_REGION_DELIVERY_UNIT[responsibleOrganisationRegion]?.contains(
            unitEnum,
          ) == true
      }
      if (!isValid) {
        throw ValidationException(ValidationErrors.ProbationDeliveryUnit.DELIVERY_UNIT_NOT_IN_REGION)
      }
    }

    order.probationDeliveryUnit = ProbationDeliveryUnit(
      versionId = order.getCurrentVersion().id,
      unit = unitInput,
    )
    return orderRepo.save(order).probationDeliveryUnit!!
  }
}
