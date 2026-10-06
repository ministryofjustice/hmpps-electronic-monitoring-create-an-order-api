package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.service.notify.NotificationClient

@Configuration
class NotifyConfiguration(@param:Value($$"${notify.api-key}") private val apiKey: String) {
  @Bean
  fun notificationClient(): NotificationClient = NotificationClient(apiKey)
}
