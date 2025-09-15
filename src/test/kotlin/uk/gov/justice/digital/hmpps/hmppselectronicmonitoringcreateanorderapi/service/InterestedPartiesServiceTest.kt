package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInterestedPartiesDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CrownCourtDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProbationServiceRegion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ResponsibleOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.YouthJusticeServiceRegions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import java.util.*

@ActiveProfiles("test")
@JsonTest
class InterestedPartiesServiceTest {

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
}
