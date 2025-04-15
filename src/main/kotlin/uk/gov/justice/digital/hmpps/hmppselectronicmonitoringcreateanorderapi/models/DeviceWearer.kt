package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Past
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "DEVICE_WEARER")
data class DeviceWearer(

  @Id
  @Column(name = "ID", nullable = false, unique = true)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "VERSION_ID", nullable = false, unique = true)
  val versionId: UUID,

  @Column(name = "NOMIS_ID", nullable = true)
  var nomisId: String? = null,

  @Column(name = "PNC_ID", nullable = true)
  var pncId: String? = null,

  @Column(name = "DELIUS_ID", nullable = true)
  var deliusId: String? = null,

  @Column(name = "HOME_OFFICE_REFERENCE_NUMBER", nullable = true)
  var homeOfficeReferenceNumber: String? = null,

  @Column(name = "PRISON_NUMBER", nullable = true)
  var prisonNumber: String? = null,

  @Column(name = "FIRST_NAME", nullable = true)
  var firstName: String? = null,

  @Column(name = "LAST_NAME", nullable = true)
  var lastName: String? = null,

  @Column(name = "ALIAS", nullable = true)
  var alias: String? = null,

  @Column(name = "ADULT_AT_TIME_OF_INSTALLATION", nullable = true)
  var adultAtTimeOfInstallation: Boolean? = null,

  @Column(name = "SEX", nullable = true)
  var sex: String? = null,

  @Column(name = "GENDER", nullable = true)
  var gender: String? = null,

  @Column(name = "LANGUAGE", nullable = true)
  var language: String? = null,

  @Column(name = "INTERPRETER_REQUIRED", nullable = true)
  var interpreterRequired: Boolean? = null,

  @Column(name = "DATE_OF_BIRTH", nullable = true)
  @field:Past(message = ValidationErrors.DeviceWearer.DOB_REQUIRED)
  var dateOfBirth: ZonedDateTime? = null,

  @Column(name = "DISABILITIES", nullable = true)
  var disabilities: String? = null,

  @Column(name = "OTHER_DISABILITY", nullable = true)
  val otherDisability: String? = null,

  @Column(name = "NO_FIXED_ABODE", nullable = true)
  var noFixedAbode: Boolean? = null,

  @Schema(hidden = true)
  @OneToOne
  @JoinColumn(name = "VERSION_ID", updatable = false, insertable = false)
  private val version: OrderVersion? = null,
) {
  val isValid: Boolean
    get() = (
      noFixedAbode != null &&
        !firstName.isNullOrBlank() &&
        !lastName.isNullOrBlank() &&
        adultAtTimeOfInstallation != null &&
        !sex.isNullOrBlank() &&
        !gender.isNullOrBlank() &&
        dateOfBirth != null &&
        noFixedAbode != null
      )
}
