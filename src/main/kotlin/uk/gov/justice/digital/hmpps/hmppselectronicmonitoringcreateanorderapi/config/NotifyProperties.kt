package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notify")
data class NotifyProperties(val templates: Templates) {
  data class Templates(val orderRejectedUser: String, val orderRejectedNo: String)
}
