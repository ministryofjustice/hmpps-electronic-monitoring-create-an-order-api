package uk.gov.justice.digital.hmpps.courthearingeventreceiver.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class PersonDetails(

  val gender: Gender,

  val lastName: String,

  val middleName: String?,

  val firstName: String?,

  val dateOfBirth: LocalDate?,

  val address: Address?,

  val contact: Contact?,

  val ethnicity: Ethnicity?,
)
enum class Gender {
  MALE,
  FEMALE,
  NOT_KNOWN,
  NOT_SPECIFIED,
}
