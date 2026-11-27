package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.Licence
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence.LicenceSummary

interface CreateAndVaryALicenceApi {
  fun getLicenceByCrn(crn: String): LicenceSummary?
  fun getLicenceByPrisonNumber(prisonNumber: String): LicenceSummary?
  fun getLicenceConditions(licenceId: String): Licence?
}
