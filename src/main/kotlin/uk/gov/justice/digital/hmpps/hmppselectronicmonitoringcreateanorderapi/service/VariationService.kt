package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.validation.ValidationException
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateVariationDetailsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.time.ZonedDateTime
import java.util.*

@Service
@Configuration
class VariationService : OrderSectionServiceBase() {
  fun updateVariationDetails(
    orderId: UUID,
    username: String,
    updateRecord: UpdateVariationDetailsDto,
  ): VariationDetails {
    val order = this.findEditableOrder(orderId, username)

    val type = VariationType.entries.firstOrNull { it.name == updateRecord.variationType }
    if (order.dataDictionaryVersion != DataDictionaryVersion.DDV4 && VariationType.DDv4_TYPES.contains(type)) {
      throw ValidationException(ValidationErrors.VariationDetails.typeObsolete(updateRecord.variationType))
    }
    with(updateRecord) {
      order.variationDetails = VariationDetails(
        versionId = order.getCurrentVersion().id,
        variationType = VariationType.valueOf(variationType),
        variationDate = ZonedDateTime.parse(variationDate),
      )
    }

    return orderRepo.save(order).variationDetails!!
  }
}
