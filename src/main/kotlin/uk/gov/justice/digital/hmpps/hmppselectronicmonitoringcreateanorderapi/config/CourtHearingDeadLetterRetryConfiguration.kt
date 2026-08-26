package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnExpression(
  "\${common-platform.dead-letter-retry.enabled:false} && \${toggle.common-platform.processing.enabled:false}",
)
class CourtHearingDeadLetterRetryConfiguration
