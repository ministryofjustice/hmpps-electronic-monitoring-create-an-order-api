package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaCategory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MappaLevel
import java.util.UUID

class Mappa(
  val id: UUID = UUID.randomUUID(),
  val versionId: UUID,
  var level: MappaLevel? = null,
  var category: MappaCategory? = null,
) {
  fun update(level: MappaLevel?, category: MappaCategory?) {
    this.level = level
    this.category = category
  }
}
