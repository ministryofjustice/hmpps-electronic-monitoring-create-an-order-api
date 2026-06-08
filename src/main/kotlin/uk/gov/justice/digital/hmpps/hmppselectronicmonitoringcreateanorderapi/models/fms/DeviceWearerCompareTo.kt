package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.DeviceWearerChange
import kotlin.collections.plusAssign

fun DeviceWearer.compareTo(previous: DeviceWearer): CompareToResult {
  val result = CompareToResult.new()

  fun compareField(change: DeviceWearerChange, newValue: Any?, oldValue: Any?) {
    if (oldValue != newValue) {
      result.addChange(change)
    }
  }

  fun <T> compareList(change: DeviceWearerChange, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      result.addChange(change)
    }
  }

  fun compareAdultChild(new: String?, old: String?) {
    if (new != old) {
      if (new == "adult") {
        result.addChange(DeviceWearerChange.ChildToAdult)
      } else if (new == "child") {
        result.addChange(DeviceWearerChange.AdultToChild)
      }
    }
  }

  fun compareNoFixedAddress(new: String?, old: String?) {
    if (new != old) {
      if (new == "true") {
        result.addChange(DeviceWearerChange.NoFixedAddress)
      } else if (new == "false") {
        result.addChange(DeviceWearerChange.HasFixedAddress)
      }
    }
  }

  compareField(DeviceWearerChange.NameChange, this.firstName, previous.firstName)
  compareField(DeviceWearerChange.NameChange, this.middleName, previous.middleName)
  compareField(DeviceWearerChange.NameChange, this.lastName, previous.lastName)
  compareField(DeviceWearerChange.Alias, this.alias, previous.alias)

  compareField(DeviceWearerChange.DateOfBirth, this.dateOfBirth, previous.dateOfBirth)
  compareAdultChild(this.adultChild, previous.adultChild)
  compareField(DeviceWearerChange.Sex, this.sex, previous.sex)
  compareField(DeviceWearerChange.GenderIdentity, this.genderIdentity, previous.genderIdentity)

  if (listOf(
      address1,
      address2,
      address3,
      address4,
      addressPostCode,
    ) != listOf(
      previous.address1,
      previous.address2,
      previous.address3,
      previous.address4,
      previous.addressPostCode,
    )
  ) {
    result.addChange(DeviceWearerChange.PrimaryAddressChange)
  }

  if (listOf(
      secondaryAddress1,
      secondaryAddress2,
      secondaryAddress3,
      secondaryAddress4,
      secondaryAddressPostCode,
    ) != listOf(
      previous.secondaryAddress1,
      previous.secondaryAddress2,
      previous.secondaryAddress3,
      previous.secondaryAddress4,
      previous.secondaryAddressPostCode,
    )
  ) {
    result.addChange(DeviceWearerChange.SecondaryAddressChange)
  }

  if (listOf(
      tertiaryAddress1,
      tertiaryAddress2,
      tertiaryAddress3,
      tertiaryAddress4,
      tertiaryAddressPostCode,
    ) != listOf(
      previous.tertiaryAddress1,
      previous.tertiaryAddress2,
      previous.tertiaryAddress3,
      previous.tertiaryAddress4,
      previous.tertiaryAddressPostCode,
    )
  ) {
    result.addChange(DeviceWearerChange.TertiaryAddressChange)
  }

  compareNoFixedAddress(this.noFixedAddress, previous.noFixedAddress)

  compareField(DeviceWearerChange.PhoneNumber, this.phoneNumber, previous.phoneNumber)

  compareField(DeviceWearerChange.Mappa, this.mappa, previous.mappa)
  compareField(DeviceWearerChange.MappaCategory, this.mappaCategory, previous.mappaCategory)
  compareField(DeviceWearerChange.MappaCaseType, this.mappaCaseType, previous.mappaCaseType)

  if (listOf(
      pncId,
      nomisId,
      deliusId,
      prisonNumber,
      complianceAndEnforcementPersonReference,
    ) != listOf(
      previous.pncId,
      previous.nomisId,
      previous.deliusId,
      previous.prisonNumber,
      previous.complianceAndEnforcementPersonReference,
    )
  ) {
    result.addChange(DeviceWearerChange.PersonalIdChanged)
  }

  if (listOf(
      interpreterRequired,
      language,
    ) != listOf(
      previous.interpreterRequired,
      previous.language,
    )
  ) {
    result.addChange(DeviceWearerChange.InterpreterRequired)
  }

  if (listOf(
      responsibleAdultRequired,
      parent,
      guardian,
    ) != listOf(
      previous.responsibleAdultRequired,
      previous.parent,
      previous.guardian,
    )
  ) {
    result.addChange(DeviceWearerChange.ResponsibleAdultChanged)
  }
  compareField(DeviceWearerChange.ParentPhoneNumber, this.parentPhoneNumber, previous.parentPhoneNumber)

  compareList(DeviceWearerChange.Disability, this.disability, previous.disability)
  compareList(DeviceWearerChange.RiskCategory, this.riskCategory, previous.riskCategory)

  return result
}

class CompareToResult(val messages: MutableList<String>, private val orderVariationTypes: MutableSet<VariationType>) {
  val orderVariationType: VariationType
    get() {
      if (this.orderVariationTypes.isEmpty()) {
        return VariationType.OTHER
      }

      return this.orderVariationTypes.minBy { it.priority }
    }

  private fun addMessage(message: String) {
    this.messages += message
  }

  private fun addOrderVariationType(variationType: VariationType) {
    this.orderVariationTypes += variationType
  }

  fun addChange(change: DeviceWearerChange) {
    this.addMessage(change.message)
    this.addOrderVariationType(change.orderVariationType)
  }

  companion object {
    fun new(): CompareToResult = CompareToResult(mutableListOf(), mutableSetOf())
  }
}
