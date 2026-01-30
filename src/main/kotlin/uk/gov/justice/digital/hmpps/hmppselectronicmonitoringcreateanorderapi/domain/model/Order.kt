package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.domain.model

import java.util.UUID

class Order(val id: UUID, val versionId: UUID, var mappa: Mappa?) {
  fun addMappa(mappa: Mappa) {
    this.mappa = mappa
  }
}
