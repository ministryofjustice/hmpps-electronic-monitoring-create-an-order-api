package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import java.util.*

class ProbationDeliveryUnitBuilder(var versionId: UUID) {
  var unit: String = "CAMDEN_AND_ISLINGTON"

  fun build(): ProbationDeliveryUnit {
    return ProbationDeliveryUnit(
      versionId = versionId,
      unit = unit,
    )
  }
}
