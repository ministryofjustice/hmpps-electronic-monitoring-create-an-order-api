package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@Validated
@ConfigurationProperties(prefix = "common-platform.dead-letter-retry")
class CourtHearingDeadLetterRetryProperties {
  var enabled: Boolean = false

  @field:Min(0)
  var maxRetries: Int = 3

  @field:Min(1)
  @field:Max(10)
  var batchSize: Int = 10

  @field:Min(1)
  @field:Max(43_200)
  var exhaustedVisibilityTimeoutSeconds: Int = 43_200
}
