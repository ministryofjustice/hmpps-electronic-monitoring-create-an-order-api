package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CivilCountyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FamilyCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MagistrateCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MilitaryCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.Prison
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.PoliceAreasDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ddv6.YouthCustodyServiceRegionDDv6
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@ActiveProfiles("test")
@JsonTest
class InterestedPartiesServiceTest : OrderSectionServiceTestBase() {

  private val orderRepo: OrderRepository = mock()
  private val addressService: AddressService = mock()
  private lateinit var service: InterestedPartiesService

  private lateinit var mockOrder: Order
  private val mockOrderId: UUID = UUID.randomUUID()
  private val mockVersionId: UUID = UUID.randomUUID()
  private val mockUsername: String = "mockUsername"

  @BeforeEach
  fun setup() {
    service = InterestedPartiesService(addressService)
    service.orderRepo = orderRepo

    mockOrder = Order(
      id = mockOrderId,
      versions = mutableListOf(
        OrderVersion(
          id = mockVersionId,
          versionId = 0,
          status = OrderStatus.IN_PROGRESS,
          orderId = mockOrderId,
          type = RequestType.REQUEST,
          username = mockUsername,
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
          interestedParties = InterestedParties(
            versionId = mockVersionId,
            responsibleOfficerName = "mockInitialOfficerName",
            responsibleOfficerPhoneNumber = "01112223333",
            responsibleOrganisation = ResponsibleOrganisation.PROBATION.name,
            responsibleOrganisationRegion = ProbationServiceRegion.LONDON.name,
            responsibleOrganisationEmail = "mockInitialRoEmail",
            notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION.name,
            notifyingOrganisationName = ProbationServiceRegion.LONDON.name,
            notifyingOrganisationEmail = "mockInitialNotifyEmail",
          ),
          probationDeliveryUnit = ProbationDeliveryUnit(
            versionId = mockVersionId,
            unit = "mockInitialPDU",
          ),
        ),
      ),
    )
  }

  @Test
  fun `should update interested parties`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.YJS,
      responsibleOrganisationRegion = YouthJusticeServiceRegions.WALES.name,
      responsibleOrganisationEmail = "mockUpdatedRoEmail",
      responsibleOfficerName = "mockUpdatedOfficerName",
      responsibleOfficerPhoneNumber = "09998887777",
      notifyingOrganisation = NotifyingOrganisationDDv5.CROWN_COURT,
      notifyingOrganisationName = CrownCourtDDv5.YORK_CROWN_COURT.name,
      notifyingOrganisationEmail = "mockUpdatedNotifyEmail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    val updatedParties = mockOrder.interestedParties!!
    assertThat(updatedParties.responsibleOrganisation).isEqualTo(mockUpdateRecord.responsibleOrganisation.toString())
    assertThat(updatedParties.responsibleOrganisationRegion).isEqualTo(mockUpdateRecord.responsibleOrganisationRegion)
    assertThat(updatedParties.responsibleOrganisationEmail).isEqualTo(mockUpdateRecord.responsibleOrganisationEmail)
    assertThat(updatedParties.responsibleOfficerName).isEqualTo(mockUpdateRecord.responsibleOfficerName)
    assertThat(updatedParties.notifyingOrganisation).isEqualTo(mockUpdateRecord.notifyingOrganisation.toString())
    assertThat(updatedParties.notifyingOrganisationName).isEqualTo(mockUpdateRecord.notifyingOrganisationName)
  }

  @Test
  fun `should clear PDU when responsible organisation changes from Probation`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    assertThat(mockOrder.probationDeliveryUnit).isNotNull()

    val mockUpdateDto = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.HOME_OFFICE,
      responsibleOrganisationRegion = "",
      responsibleOrganisationEmail = "mockHoEmail",
      responsibleOfficerName = "mockHoOfficer",
      responsibleOfficerPhoneNumber = "01234567890",
      notifyingOrganisation = NotifyingOrganisationDDv5.HOME_OFFICE,
      notifyingOrganisationName = "",
      notifyingOrganisationEmail = "mockHoNotifyEmail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateDto)

    assertThat(mockOrder.probationDeliveryUnit).isNull()
    assertThat(mockOrder.interestedParties?.responsibleOrganisation).isEqualTo(ResponsibleOrganisation.HOME_OFFICE.name)
  }

  @Test
  fun `should clear PDU when responsible organisation is Probation but region changes`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    assertThat(mockOrder.probationDeliveryUnit).isNotNull()

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.PROBATION,
      responsibleOrganisationRegion = ProbationServiceRegion.WALES.name,
      responsibleOrganisationEmail = "mockProbationEmail",
      responsibleOfficerName = "mockProbationOfficer",
      responsibleOfficerPhoneNumber = "09876543210",
      notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION,
      notifyingOrganisationName = ProbationServiceRegion.WALES.name,
      notifyingOrganisationEmail = "mockProbationNotifyEmail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    assertThat(mockOrder.probationDeliveryUnit).isNull()
  }

  @Test
  fun `Should save empty name when notifying organisation is PROBATION without a notifying org name`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.PROBATION,
      responsibleOrganisationRegion = ProbationServiceRegion.LONDON.name,
      responsibleOrganisationEmail = "mockProbationEmail",
      responsibleOfficerName = "mockProbationOfficer",
      responsibleOfficerPhoneNumber = "09876543210",
      notifyingOrganisation = NotifyingOrganisationDDv5.PROBATION,
      notifyingOrganisationName = "",
      notifyingOrganisationEmail = "mockProbationNotifyEmail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    assertThat(mockOrder.interestedParties?.notifyingOrganisationName).isEqualTo("")
  }

  @Test
  fun `Should save empty name when notifying organisation is YCS`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.YJS,
      responsibleOrganisationRegion = YouthJusticeServiceRegions.WALES.name,
      responsibleOrganisationEmail = "mockEmail",
      responsibleOfficerName = "mockOfficer",
      responsibleOfficerPhoneNumber = "09876543210",
      notifyingOrganisation = NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE,
      notifyingOrganisationName = "",
      notifyingOrganisationEmail = "mockemail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    assertThat(mockOrder.interestedParties?.notifyingOrganisationName).isEqualTo("")
  }

  @Test
  fun `Should save responsible organisation as police in ddv6`() {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.POLICE,
      responsibleOrganisationRegion = PoliceAreasDDv6.METROPOLITAN_POLICE.name,
      responsibleOrganisationEmail = "mockEmail",
      responsibleOfficerName = "mockOfficer",
      responsibleOfficerPhoneNumber = "09876543210",
      notifyingOrganisation = NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE,
      notifyingOrganisationName = "",
      notifyingOrganisationEmail = "mockemail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    assertThat(mockOrder.interestedParties?.responsibleOrganisation).isEqualTo("POLICE")
    assertThat(mockOrder.interestedParties?.responsibleOrganisationRegion).isEqualTo("METROPOLITAN_POLICE")
  }

  @ParameterizedTest(name = "it should save owner cohort - {0} -> {1}")
  @MethodSource("ownerCohortValues")
  fun `Should save owner cohort as notifying organisation name when notifying organisation is prison`(
    notifyingOrganisation: NotifyingOrganisationDDv5,
    notifyingOrganisationName: String,
    expectedOwnerName: String,
  ) {
    whenever(orderRepo.findById(mockOrderId)).thenReturn(Optional.of(mockOrder))
    whenever(orderRepo.save(mockOrder)).thenReturn(mockOrder)

    val mockUpdateRecord = UpdateInterestedPartiesDto(
      responsibleOrganisation = ResponsibleOrganisation.POLICE,
      responsibleOrganisationRegion = PoliceAreasDDv6.METROPOLITAN_POLICE.name,
      responsibleOrganisationEmail = "mockEmail",
      responsibleOfficerName = "mockOfficer",
      responsibleOfficerPhoneNumber = "09876543210",
      notifyingOrganisation = notifyingOrganisation,
      notifyingOrganisationName = notifyingOrganisationName,
      notifyingOrganisationEmail = "mockemail",
    )

    service.updateInterestedParties(mockOrderId, mockUsername, mockUpdateRecord)

    argumentCaptor<Order>().apply {
      verify(orderRepo, times(1)).save(capture())
      assertThat(firstValue.ownerCohort).isEqualTo(expectedOwnerName)
    }
  }

  companion object {
    @JvmStatic
    fun ownerCohortValues() = listOf(
      Arguments.of(NotifyingOrganisationDDv5.PRISON, Prison.LEWES_PRISON.name, Prison.LEWES_PRISON.name),
      Arguments.of(
        NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE,
        YouthCustodyServiceRegionDDv6.LONDON.name,
        NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name,
      ),
      Arguments.of(
        NotifyingOrganisationDDv5.PROBATION,
        ProbationServiceRegion.LONDON.name,
        NotifyingOrganisationDDv5.PROBATION.name,
      ),
      Arguments.of(NotifyingOrganisationDDv5.HOME_OFFICE, "Home Office", NotifyingOrganisationDDv5.HOME_OFFICE.name),
      Arguments.of(
        NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT,
        CivilCountyCourtDDv5.ALDERSHOT_COUNTY_AND_CIVIL_COURT.name,
        NotifyingOrganisationDDv5.CIVIL_COUNTY_COURT.name,
      ),
      Arguments.of(
        NotifyingOrganisationDDv5.CROWN_COURT,
        CrownCourtDDv5.YORK_CROWN_COURT.name,
        NotifyingOrganisationDDv5.CROWN_COURT.name,
      ),
      Arguments.of(
        NotifyingOrganisationDDv5.MAGISTRATES_COURT,
        MagistrateCourtDDv5.WELLINGBOROUGH_MAGISTRATES_COURT.name,
        NotifyingOrganisationDDv5.MAGISTRATES_COURT.name,
      ),
      Arguments.of(
        NotifyingOrganisationDDv5.MILITARY_COURT,
        MilitaryCourtDDv5.CATTERICK_MILITARY_COURT_CENTRE.name,
        NotifyingOrganisationDDv5.MILITARY_COURT.name,
      ),
      Arguments.of(NotifyingOrganisationDDv5.SCOTTISH_COURT, "", NotifyingOrganisationDDv5.SCOTTISH_COURT.name),
      Arguments.of(
        NotifyingOrganisationDDv5.FAMILY_COURT,
        FamilyCourtDDv5.COURT_OF_PROTECTION_COURT_FAMILY_COURT.name,
        NotifyingOrganisationDDv5.FAMILY_COURT.name,
      ),
      Arguments.of(
        NotifyingOrganisationDDv5.YOUTH_COURT,
        YouthCourtDDv5.ELY_YOUTH_COURT.name,
        NotifyingOrganisationDDv5.YOUTH_COURT.name,
      ),
    )
  }
}
