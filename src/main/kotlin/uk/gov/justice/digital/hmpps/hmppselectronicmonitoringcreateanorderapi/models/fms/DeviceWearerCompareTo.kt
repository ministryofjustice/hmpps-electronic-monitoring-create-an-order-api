package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.config.DeviceWearerChange
import kotlin.collections.plusAssign

fun DeviceWearer.compareTo(previous: DeviceWearer): CompareToResult {
  val messages = mutableListOf<String>()
  val orderVariationTypes: MutableSet<VariationType> = mutableSetOf()

  fun compareField(change: DeviceWearerChange, newValue: Any?, oldValue: Any?) {
    if (oldValue != newValue) {
      messages += change.message
      orderVariationTypes += change.orderVariationType
    }
  }

  fun <T> compareList(change: DeviceWearerChange, new: List<T>?, old: List<T>?) {
    if ((old ?: emptyList()) != (new ?: emptyList<T>())) {
      messages += change.message
      orderVariationTypes += change.orderVariationType
    }
  }

  fun compareAdultChild(new: String?, old: String?) {
    if (new != old) {
      if (new == "adult") {
        messages += DeviceWearerChange.ChildToAdult.message
        orderVariationTypes += DeviceWearerChange.ChildToAdult.orderVariationType
      } else if (new == "child") {
        messages += DeviceWearerChange.AdultToChild.message
        orderVariationTypes += DeviceWearerChange.AdultToChild.orderVariationType
      }
    }
  }

  fun compareNoFixedAddress(new: String?, old: String?) {
    if (new != old) {
      if (new == "true") {
        messages += DeviceWearerChange.NoFixedAddress.message
        orderVariationTypes += DeviceWearerChange.NoFixedAddress.orderVariationType
      } else if (new == "false") {
        messages += DeviceWearerChange.HasFixedAddress.message
        orderVariationTypes += DeviceWearerChange.HasFixedAddress.orderVariationType
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
    messages += DeviceWearerChange.PrimaryAddressChange.message
    orderVariationTypes += DeviceWearerChange.PrimaryAddressChange.orderVariationType
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
    messages += DeviceWearerChange.SecondaryAddressChange.message
    orderVariationTypes += DeviceWearerChange.SecondaryAddressChange.orderVariationType
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
    messages += DeviceWearerChange.TertiaryAddressChange.message
    orderVariationTypes += DeviceWearerChange.TertiaryAddressChange.orderVariationType
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
    messages += DeviceWearerChange.PersonalIdChanged.message
    orderVariationTypes += DeviceWearerChange.PersonalIdChanged.orderVariationType
  }

  if (listOf(
      interpreterRequired,
      language,
    ) != listOf(
      previous.interpreterRequired,
      previous.language,
    )
  ) {
    messages += DeviceWearerChange.InterpreterRequired.message
    orderVariationTypes += DeviceWearerChange.InterpreterRequired.orderVariationType
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
    messages += DeviceWearerChange.ResponsibleAdultChanged.message
    orderVariationTypes += DeviceWearerChange.ResponsibleAdultChanged.orderVariationType
  }
  compareField(DeviceWearerChange.ParentPhoneNumber, this.parentPhoneNumber, previous.parentPhoneNumber)

  compareList(DeviceWearerChange.Disability, this.disability, previous.disability)
  compareList(DeviceWearerChange.RiskCategory, this.riskCategory, previous.riskCategory)

  return CompareToResult(messages, orderVariationTypes)
}

class CompareToResult(val messages: List<String>, private val orderVariationTypes: Set<VariationType>) {
  val orderVariationType: VariationType
    get() {
      if (this.orderVariationTypes.isEmpty()) {
        return VariationType.OTHER
      }

      return this.orderVariationTypes.minBy { it.priority }
    }
}
