package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

interface EmailClient {
  fun sendUserEmail()
  fun sendNotificationOfficerEmail()
}
