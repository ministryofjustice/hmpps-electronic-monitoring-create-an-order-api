package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests

import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Disability
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.requests.components.FmsDisability
import java.time.format.DateTimeFormatter

class DeviceWearerRequestOnlineFormAdaptor(private val order: Order) : DeviceWearerRequest() {

  override val firstName: String
    get() {
      return order.deviceWearer?.firstName ?: super.firstName
    }

  override val lastName: String
    get() {
      return order.deviceWearer?.lastName ?: super.lastName
    }

  override val alias: String?
    get() {
      return order.deviceWearer?.alias ?: super.alias
    }

  override val dateOfBirth: String
    get() {
      return order.deviceWearer?.dateOfBirth?.format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
      ) ?: super.dateOfBirth
    }

  override val adultChild: String
    get() {
      if (!order.deviceWearer?.adultAtTimeOfInstallation!!) {
        return "child"
      }
      return "adult"
    }

  override val sex: String
    get() {
      return order.deviceWearer?.sex ?: super.sex
    }

  override val genderIdentity: String
    get() {
      return order.deviceWearer?.gender ?: super.genderIdentity
    }

  override val disability: List<FmsDisability>
    get() {
      if (order.deviceWearer?.disabilities.isNullOrEmpty()) {
        return emptyList()
      }
      return Disability
        .getValuesFromEnumString(order.deviceWearer!!.disabilities!!)
        .map { disability -> FmsDisability(disability) }
    }

  override val address1: String
    get() {
      if (order.deviceWearer?.noFixedAbode == false) {
        return order.addresses.find { address ->
          address.addressType == AddressType.PRIMARY
        }?.addressLine1 ?: super.address1
      }
      return super.address1
    }

  override val address2: String
    get() {
      if (order.deviceWearer?.noFixedAbode == false) {
        return order.addresses.find { address ->
          address.addressType == AddressType.PRIMARY
        }?.addressLine2 ?: super.address2
      }
      return super.address2
    }

  override val address3: String
    get() {
      if (order.deviceWearer?.noFixedAbode == false) {
        return order.addresses.find { address ->
          address.addressType == AddressType.PRIMARY
        }?.addressLine3 ?: super.address3
      }
      return super.address3
    }

  override val address4: String
    get() {
      if (order.deviceWearer?.noFixedAbode == false) {
        return order.addresses.find { address ->
          address.addressType == AddressType.PRIMARY
        }?.addressLine4 ?: super.address4
      }
      return super.address4
    }

  override val addressPostCode: String
    get() {
      if (order.deviceWearer?.noFixedAbode == false) {
        return order.addresses.find { address ->
          address.addressType == AddressType.PRIMARY
        }?.postcode ?: super.addressPostCode
      }
      return super.addressPostCode
    }

  override val secondaryAddress1: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.SECONDARY
      }?.addressLine1 ?: super.secondaryAddress1
    }

  override val secondaryAddress2: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.SECONDARY
      }?.addressLine2 ?: super.secondaryAddress2
    }

  override val secondaryAddress3: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.SECONDARY
      }?.addressLine3 ?: super.secondaryAddress3
    }

  override val secondaryAddress4: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.SECONDARY
      }?.addressLine4 ?: super.secondaryAddress4
    }

  override val secondaryAddressPostCode: String
    get() {
      return order.addresses.find { address ->
        address.addressType == AddressType.SECONDARY
      }?.postcode ?: super.secondaryAddressPostCode
    }

  override val phoneNumber: String
    get() {
      return order.contactDetails?.contactNumber ?: super.phoneNumber
    }

  override val riskDetails: String?
    get() {
      return order.installationAndRisk?.riskDetails ?: super.riskDetails
    }

  override val mappa: String?
    get() {
      return order.installationAndRisk?.mappaLevel ?: super.mappa
    }

  override val mappaCaseType: String?
    get() {
      return order.installationAndRisk?.mappaCaseType ?: super.mappaCaseType
    }

  override val responsibleAdultRequired: String
    get() {
      if (order.deviceWearerResponsibleAdult == null) {
        return "false"
      }
      return "true"
    }

  override val parent: String
    get() {
      return order.deviceWearerResponsibleAdult?.fullName ?: super.parent
    }

  override val parentPhoneNumber: String?
    get() {
      return order.deviceWearerResponsibleAdult?.contactNumber ?: super.parentPhoneNumber
    }

  override val pncId: String?
    get() {
      return order.deviceWearer?.pncId ?: super.pncId
    }

  override val nomisId: String?
    get() {
      return order.deviceWearer?.nomisId ?: super.nomisId
    }

  override val deliusId: String?
    get() {
      return order.deviceWearer?.deliusId ?: super.deliusId
    }

  override val prisonNumber: String?
    get() {
      return order.deviceWearer?.prisonNumber ?: super.prisonNumber
    }

  override val homeOfficeReferenceNumber: String?
    get() {
      return order.deviceWearer?.homeOfficeReferenceNumber ?: super.homeOfficeReferenceNumber
    }

  override val interpreterRequired: String?
    get() {
      return order.deviceWearer?.interpreterRequired?.toString() ?: super.interpreterRequired
    }

  override val language: String?
    get() {
      return order.deviceWearer?.language ?: super.language
    }
}
