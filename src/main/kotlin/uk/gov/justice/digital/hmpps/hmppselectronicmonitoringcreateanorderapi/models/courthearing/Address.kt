package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(

  val address1: String,

  val address2: String?,

  val address3: String?,

  val address4: String?,

  val address5: String?,

  val postcode: String?,
)
