package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "settings")
class FeatureFlags(
  @Value("\${data-dictionary-version}") val dataDictionaryVersion: String,
  @Value("\${ddv6-court-mappings:false") val ddV6CourtMappings: Boolean,
)
