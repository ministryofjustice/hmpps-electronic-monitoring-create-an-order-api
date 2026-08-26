package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.client

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordAuthorisationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordDependencyException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CorePersonRecordNotFoundException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Alias
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.CodeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.Contact
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.corePersonRecord.CorePersonDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDates.londonTimeZone
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address as CemoAddress

@Component
class CorePersonRecordApiClient(private val corePersonRecordApiWebClient: WebClient) : CorePersonRecordApi {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  }

  override fun getPersonByPrisonNumber(prisonNumber: String, versionId: UUID): CorePersonRecord {
    val details = fetchCorePersonDetails("/person/prison/{identifier}", prisonNumber)

    return mapToCorePersonRecord(details, versionId)
  }

  override fun getPersonByCrn(crn: String, versionId: UUID): CorePersonRecord {
    val details = fetchCorePersonDetails("/person/probation/{identifier}", crn)

    return mapToCorePersonRecord(details, versionId)
  }

  private fun fetchCorePersonDetails(path: String, identifier: String): CorePersonDetails = try {
    corePersonRecordApiWebClient
      .get()
      .uri(path, identifier)
      .retrieve()
      .bodyToMono<CorePersonDetails>()
      .retryWhen(
        Retry.fixedDelay(1, Duration.ofMillis(2000))
          // Only retry on 5xx/429/408 status error, do not retry for bad request
          .filter {
            it is WebClientResponseException &&
              (
                it.statusCode.is5xxServerError ||
                  it.statusCode.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS) ||
                  it.statusCode.isSameCodeAs(HttpStatus.REQUEST_TIMEOUT)
                )
          },
      )
      .block()!!
  } catch (error: WebClientResponseException.NotFound) {
    throw CorePersonRecordNotFoundException(
      "No Core Person Record was found for id $identifier",
      error,
    )
  } catch (error: WebClientResponseException.Forbidden) {
    throw CorePersonRecordAuthorisationException(
      "Core Person Record authorisation failed for id $identifier",
      error,
    )
  } catch (error: WebClientResponseException) {
    throw CorePersonRecordDependencyException(
      "Core Person Record lookup failed for id $identifier",
      error.statusCode,
      error,
    )
  } catch (error: WebClientRequestException) {
    throw CorePersonRecordDependencyException(
      "Core Person Record is unavailable for id $identifier",
      null,
      error,
    )
  }

  internal fun mapToCorePersonRecord(details: CorePersonDetails, versionId: UUID): CorePersonRecord = CorePersonRecord(
    deviceWearer = toDeviceWearer(details, versionId),
    contactDetails = toContactDetails(details, versionId),
    addresses = listOfNotNull(toPrimaryAddress(details, versionId), toSecondaryAddress(details, versionId)),
  )

  private fun toDeviceWearer(details: CorePersonDetails, versionId: UUID): DeviceWearer = DeviceWearer(
    versionId = versionId,
    firstName = details.firstName,
    middleName = details.middleNames,
    lastName = details.lastName,
    prisonNumber = details.identifiers?.prisonNumbers?.firstOrNull(),
    courtCaseReferenceNumber = details.identifiers?.crns?.firstOrNull(),
    pncId = details.identifiers?.pncs?.firstOrNull(),
    nationalInsuranceNumber = details.identifiers?.nationalInsuranceNumbers?.firstOrNull(),
    dateOfBirth = parsedDateOfBirth(details.dateOfBirth),
    sex = toSex(details.sex),
    alias = details.aliases?.firstOrNull()?.let(::toAlias),
    noFixedAbode = details.addresses?.any { it.noFixedAbode ?: false },
    religion = details.religion?.description,
    nationality = details.nationalities.firstOrNull()?.description,
    ethnicity = details.ethnicity?.description,
  )

  private fun toPrimaryAddress(details: CorePersonDetails, versionId: UUID): CemoAddress? {
    val primaryAddress = details.addresses?.firstOrNull { isPrimaryAddress(it) } ?: return null

    return toCemoAddress(primaryAddress, versionId)
  }

  private fun toSecondaryAddress(details: CorePersonDetails, versionId: UUID): CemoAddress? {
    val secondaryAddress = details.addresses?.firstOrNull { isSecondaryAddress(it) } ?: return null

    return toCemoAddress(secondaryAddress, versionId)
  }

  private fun toContactDetails(details: CorePersonDetails, versionId: UUID): ContactDetails? {
    val contacts = details.addresses?.flatMap { it.contacts } ?: return null
    val matchingContact = contacts.firstOrNull { isMobile(it) } ?: contacts.firstOrNull { isHome(it) } ?: return null

    return ContactDetails(
      versionId = versionId,
      contactNumber = matchingContact.value,
      phoneNumberAvailable = true,
    )
  }

  private fun toCemoAddress(address: Address, versionId: UUID): CemoAddress = CemoAddress(
    versionId = versionId,
    addressLine1 = addressLineOne(address),
    addressLine2 = "",
    addressLine3 = address.postTown?.toTitleCase() ?: "",
    addressLine4 = address.county?.toTitleCase() ?: "",
    postcode = address.postcode ?: "",
    addressType = if (isPrimaryAddress(address)) AddressType.PRIMARY else AddressType.SECONDARY,
  )

  private fun isPrimaryAddress(address: Address): Boolean = address.status?.code == "M"

  private fun isSecondaryAddress(address: Address): Boolean = address.status?.code == "S"

  private fun isMobile(contact: Contact): Boolean = contact.type?.code == "MOBILE"

  private fun isHome(contact: Contact): Boolean = contact.type?.code == "HOME"

  private fun toSex(sex: CodeDescription?): String? = when (sex?.code) {
    "M" -> "MALE"
    "F" -> "FEMALE"
    "NS" -> "PREFER_NOT_TO_SAY"
    else -> "UNKNOWN"
  }

  private fun toAlias(alias: Alias): String = "${alias.firstName} ${alias.middleNames} ${alias.lastName}"

  private fun addressLineOne(address: Address): String {
    val buildingId = address.buildingNumber.takeIf { !it.isNullOrEmpty() }
      ?: address.buildingName.takeIf { !it.isNullOrEmpty() } ?: ""

    if (address.thoroughfareName.isNullOrEmpty()) {
      return buildingId
    }

    return "$buildingId ${address.thoroughfareName}".toTitleCase()
  }

  private fun parsedDateOfBirth(dateOfBirth: String?): ZonedDateTime? = parseDateOrNull(dateOfBirth ?: "")

  private fun parseDate(date: String): ZonedDateTime =
    LocalDate.parse(date, dateFormatter).atStartOfDay().atZone(londonTimeZone)

  private fun parseDateOrNull(date: String): ZonedDateTime? = if (date.isNotBlank()) parseDate(date) else null

  private fun String.toTitleCase(): String = lowercase().split(" ").joinToString(" ") {
    it.replaceFirstChar { char -> char.uppercaseChar() }
  }
}
