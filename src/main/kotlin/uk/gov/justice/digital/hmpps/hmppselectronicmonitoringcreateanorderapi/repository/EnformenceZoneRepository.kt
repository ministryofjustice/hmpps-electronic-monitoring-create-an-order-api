package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import java.util.*

interface EnformenceZoneRepository : JpaRepository<EnforcementZoneConditions, UUID>
