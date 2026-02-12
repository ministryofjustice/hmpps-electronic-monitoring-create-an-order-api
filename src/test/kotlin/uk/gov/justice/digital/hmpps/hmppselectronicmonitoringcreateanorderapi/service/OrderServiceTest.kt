package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.NotifyingOrganisationDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.PrisonDDv5
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsMonitoringOrderSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderListSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.specification.OrderSearchSpecification
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@JsonTest
class OrderServiceTest {
  private lateinit var repo: OrderRepository
  private lateinit var fmsService: FmsService
  private lateinit var service: OrderService
  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)

  @BeforeEach
  fun setup() {
    repo = mock(OrderRepository::class.java)
    fmsService = mock(FmsService::class.java)
    service = OrderService(repo, fmsService, "DDV4")
  }

  @Test
  fun `Create a new order for user and save to database`() {
    val result = service.createOrder("mockUser", CreateOrderDto())

    assertThat(result.id).isNotNull()
    assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    assertThat(result.username).isEqualTo("mockUser")
    assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    assertThat(result.dataDictionaryVersion).isEqualTo(DataDictionaryVersion.DDV4)
    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue).isEqualTo(result)
    }
  }

  @Test
  fun `Should save the name of the user who submitted the order`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
    )
    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.getCurrentVersion().submittedBy).isEqualTo("mockName")
    }
  }

  @Test
  fun `Should create fms device wearer and monitoring order and save both id to database`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
    )
    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.fmsResultId).isEqualTo(mockFmsResult.id)
    }
  }

  @Test
  fun `Should add prison and prison name to tags`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
      notifyingOrganisation = NotifyingOrganisation.PRISON.name,
      notifyingOrganisationName = PrisonDDv5.BEDFORD_PRISON.name,
    )
    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.tags!!.split(',')).contains("PRISON")
      assertThat(firstValue.tags!!.split(',')).contains("BEDFORD_PRISON")
    }
  }

  @Test
  fun `Should add Youth YOI to tags when notifying org is Prison and responsible adult required`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
      notifyingOrganisation = NotifyingOrganisation.PRISON.name,
      notifyingOrganisationName = PrisonDDv5.BEDFORD_PRISON.name,
    )

    mockOrder.deviceWearer!!.adultAtTimeOfInstallation = false

    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.tags!!.split(',')).contains("PRISON")
      assertThat(firstValue.tags!!.split(',')).contains("BEDFORD_PRISON")
      assertThat(firstValue.tags!!.split(',')).contains("Youth YOI")
    }
  }

  @Test
  fun `Should add PROBATION to tags when notifying org is Probation`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
      notifyingOrganisation = NotifyingOrganisation.PROBATION.name,
    )

    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )

    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(mockFmsResult)

    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.tags).isEqualTo("PROBATION")
    }
  }

  @Test
  fun `Should add Youth YCS to tags when notifying org is YCS and responsible adult required`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(
      startDate = mockStartDate,
      endDate = mockEndDate,
      username = "mockUser",
      notifyingOrganisation = NotifyingOrganisationDDv5.YOUTH_CUSTODY_SERVICE.name,
    )

    mockOrder.deviceWearer!!.adultAtTimeOfInstallation = false

    reset(repo)

    val mockFmsResult = FmsSubmissionResult(
      orderId = mockOrder.getCurrentVersion().id,
      deviceWearerResult = FmsDeviceWearerSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        deviceWearerId = "mockDeviceWearerId",
      ),
      monitoringOrderResult = FmsMonitoringOrderSubmissionResult(
        status = SubmissionStatus.SUCCESS,
        monitoringOrderId = "mockMonitoringOrderId",
      ),
      orderSource = FmsOrderSource.CEMO,
      strategy = FmsSubmissionStrategyKind.ORDER,
    )
    whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
    whenever(fmsService.submitOrder(any<Order>(), eq(FmsOrderSource.CEMO))).thenReturn(
      mockFmsResult,
    )
    service.submitOrder(mockOrder.id, "mockUser", "mockName")

    argumentCaptor<Order>().apply {
      verify(repo, times(1)).save(capture())
      assertThat(firstValue.tags!!.split(',')).contains("Youth YCS")
    }
  }

  @Test
  fun `Should be able to list all orders`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(startDate = mockStartDate, endDate = mockEndDate)
    val mockCriteria = OrderListCriteria(username = "test")

    whenever(repo.findAll(ArgumentMatchers.any(OrderListSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.listOrders(mockCriteria)

    assertThat(result).isEqualTo(listOf(mockOrder))
  }

  @Test
  fun `Should be able to search for orders`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(startDate = mockStartDate, endDate = mockEndDate)
    val mockCriteria = OrderSearchCriteria(searchTerm = "Bob Smith")

    whenever(repo.findAll(ArgumentMatchers.any(OrderSearchSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.searchOrders(mockCriteria)

    assertThat(result).isEqualTo(listOf(mockOrder))
  }

  @Nested
  @DisplayName("Create Version")
  inner class CreateVersion {
    val originalVersionId: UUID = UUID.randomUUID()
    val mockLicenceDocumentId: UUID = UUID.randomUUID()
    val mockPhotoDocumentId: UUID = UUID.randomUUID()
    val order = TestUtilities.createReadyToSubmitOrder(
      versionId = originalVersionId,
      startDate = mockStartDate,
      endDate = mockEndDate,
      status = OrderStatus.SUBMITTED,
      dataDictionaryVersion = DataDictionaryVersion.DDV4,
      installationLocation = InstallationLocation(
        versionId = originalVersionId,
        location = InstallationLocationType.PRISON,
      ),
      installationAppointment = InstallationAppointment(
        versionId = originalVersionId,
        appointmentDate = mockStartDate,
      ),
      documents = mutableListOf(
        AdditionalDocument(
          versionId = originalVersionId,
          fileName = "MockFile",
          fileType = DocumentType.LICENCE,
          documentId = mockLicenceDocumentId,
        ),
        AdditionalDocument(
          versionId = originalVersionId,
          fileName = "MockPhotoFile",
          fileType = DocumentType.PHOTO_ID,
          documentId = mockPhotoDocumentId,
        ),
      ),
    )
    val originalVersion = order.versions.find { it.id == originalVersionId }

    @BeforeEach
    fun setup() {
      service = OrderService(repo, fmsService, "DDV5")
      whenever(repo.findById(order.id)).thenReturn(Optional.of(order))
      whenever(repo.save(order)).thenReturn(order)
    }

    @Nested
    @DisplayName("Create Version as Variation")
    inner class CreateVersionAsVariation {
      @BeforeEach
      fun setup() {
        service.createVersion(order.id, order.username, RequestType.VARIATION)
      }

      @Test
      fun `It should create an new order version with type VARIATION`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.id).isEqualTo(order.id)
          assertThat(firstValue.versions.count()).isEqualTo(2)
          assertThat(firstValue.versions.last().id).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().status).isEqualTo(OrderStatus.IN_PROGRESS)
          assertThat(firstValue.versions.last().type).isEqualTo(RequestType.VARIATION)
          assertThat(firstValue.versions.last().username).isEqualTo(order.username)
          assertThat(firstValue.versions.last().versionId).isEqualTo(1)
        }
      }

      @Test
      fun `It should create an new order version with current default data dictionary version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.id).isEqualTo(order.id)

          assertThat(firstValue.versions.last().dataDictionaryVersion).isEqualTo(DataDictionaryVersion.DDV5)
        }
      }

      @Test
      fun `It should create an new order version with type VARIATION and original version is not modified`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.first())
            .isEqualTo(originalVersion)
        }
      }

      @Test
      fun `It should create an new order version with empty variation details`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().variationDetails).isNull()
        }
      }

      @Test
      fun `It should clone orderParameters referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().orderParameters).isNotNull()
          assertThat(firstValue.versions.last().orderParameters?.versionId)
            .isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().orderParameters?.versionId)
            .isNotEqualTo(originalVersionId)
          assertThat(
            firstValue.versions.last().orderParameters?.havePhoto,
          ).isEqualTo(originalVersion?.orderParameters?.havePhoto)
        }
      }

      @Test
      fun `It should clone deviceWearer referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().deviceWearer).isNotNull()
          assertThat(firstValue.versions.last().deviceWearer?.versionId).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().deviceWearer?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().deviceWearer)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.deviceWearer)
        }
      }

      @Test
      fun `It should clone deviceWearerResponsibleAdult referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().deviceWearerResponsibleAdult).isNotNull()
          assertThat(
            firstValue.versions.last().deviceWearerResponsibleAdult?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().deviceWearerResponsibleAdult?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().deviceWearerResponsibleAdult)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.deviceWearerResponsibleAdult)
        }
      }

      @Test
      fun `It should clone contactDetails referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().contactDetails).isNotNull()
          assertThat(firstValue.versions.last().contactDetails?.versionId)
            .isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().contactDetails?.versionId)
            .isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().contactDetails)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.contactDetails)
        }
      }

      @Test
      fun `It should clone curfewConditions referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().curfewConditions).isNotNull()
          assertThat(firstValue.versions.last().curfewConditions?.versionId).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().curfewConditions?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().curfewConditions)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.curfewConditions)
        }
      }

      @Test
      fun `It should clone curfewReleaseDateConditions referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().curfewReleaseDateConditions).isNotNull()
          assertThat(
            firstValue.versions.last().curfewReleaseDateConditions?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().curfewReleaseDateConditions?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().curfewReleaseDateConditions)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.curfewReleaseDateConditions)
        }
      }

      @Test
      fun `It should clone installationAndRisk referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().installationAndRisk).isNotNull()
          assertThat(firstValue.versions.last().installationAndRisk?.versionId).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().installationAndRisk?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().installationAndRisk)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.installationAndRisk)
        }
      }

      @Test
      fun `It should clone interestedParties referencing new version and clear notifying organisation data`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().interestedParties).isNotNull()
          assertThat(firstValue.versions.last().interestedParties?.versionId).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().interestedParties?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().interestedParties?.notifyingOrganisation).isNull()
          assertThat(firstValue.versions.last().interestedParties?.notifyingOrganisationName).isNull()
          assertThat(firstValue.versions.last().interestedParties?.notifyingOrganisationEmail).isNull()
          assertThat(firstValue.versions.last().interestedParties)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
              "notifyingOrganisation",
              "notifyingOrganisationName",
              "notifyingOrganisationEmail",
            )
            .isEqualTo(originalVersion?.interestedParties)
        }
      }

      @Test
      fun `It should clone probationDeliveryUnit referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().probationDeliveryUnit).isNotNull()
          assertThat(
            firstValue.versions.last().probationDeliveryUnit?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().probationDeliveryUnit?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().probationDeliveryUnit)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.probationDeliveryUnit)
        }
      }

      @Test
      fun `It should clone monitoringConditions referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().monitoringConditions).isNotNull()
          assertThat(
            firstValue.versions.last().monitoringConditions?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().monitoringConditions?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().monitoringConditions)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
              "startDate",
              "endDate",
            )
            .isEqualTo(originalVersion?.monitoringConditions)

          assertThat(firstValue.versions.last().monitoringConditions!!.startDate).isNull()
          assertThat(firstValue.versions.last().monitoringConditions!!.endDate).isNull()
        }
      }

      @Test
      fun `It should clone installationLocation referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(
            firstValue.versions.last().installationLocation?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().installationLocation?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().installationLocation)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.installationLocation)
        }
      }

      @Test
      fun `It should clone installationAppointment referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(
            firstValue.versions.last().installationAppointment?.versionId,
          ).isEqualTo(firstValue.versions.last().id)
          assertThat(firstValue.versions.last().installationAppointment?.versionId).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().installationAppointment)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields(
              "id",
              "versionId",
              "version",
            )
            .isEqualTo(originalVersion?.installationAppointment)
        }
      }

      @Test
      fun `It should clone additionalDocuments referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().additionalDocuments.count()).isEqualTo(2)

          firstValue.versions.last().additionalDocuments.forEach {
            assertThat(it.versionId).isEqualTo(firstValue.versions.last().id)
            assertThat(it.versionId).isNotEqualTo(originalVersionId)
          }
          for (i in firstValue.versions.last().additionalDocuments.indices) {
            val file = firstValue.versions.last().additionalDocuments[i]
            val originalFile = originalVersion?.additionalDocuments[i]
            assertThat(file)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields(
                "id",
                "versionId",
                "version",
              )
              .isEqualTo(originalFile)
          }
        }
      }

      @Test
      fun `It should clone addresses referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.versions.last().addresses.count()).isEqualTo(3)

          firstValue.versions.last().addresses.forEach {
            assertThat(it.versionId).isEqualTo(firstValue.versions.last().id)
            assertThat(it.versionId).isNotEqualTo(originalVersionId)
          }
          for (i in firstValue.versions.last().addresses.indices) {
            val file = firstValue.versions.last().addresses[i]
            val originalFile = originalVersion?.addresses[i]
            assertThat(file)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields(
                "id",
                "versionId",
                "version",
              )
              .isEqualTo(originalFile)
          }
        }
      }

      @Test
      fun `It should clone curfewTimeTable referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(
            firstValue.versions.last().curfewTimeTable.count(),
          ).isEqualTo(originalVersion?.curfewTimeTable?.count())

          firstValue.versions.last().curfewTimeTable.forEach {
            assertThat(it.versionId).isEqualTo(firstValue.versions.last().id)
            assertThat(it.versionId).isNotEqualTo(originalVersionId)
          }
          for (i in firstValue.versions.last().curfewTimeTable.indices) {
            val file = firstValue.versions.last().curfewTimeTable[i]
            val originalFile = originalVersion?.curfewTimeTable[i]
            assertThat(file)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields(
                "id",
                "versionId",
                "version",
              )
              .isEqualTo(originalFile)
          }
        }
      }

      @Test
      fun `It should clone enforcementZoneConditions referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(
            firstValue.versions.last().enforcementZoneConditions.count(),
          ).isEqualTo(originalVersion?.enforcementZoneConditions?.count())

          firstValue.versions.last().enforcementZoneConditions.forEach {
            assertThat(it.versionId).isEqualTo(firstValue.versions.last().id)
            assertThat(it.versionId).isNotEqualTo(originalVersionId)
          }
          for (i in firstValue.versions.last().enforcementZoneConditions.indices) {
            val file = firstValue.versions.last().enforcementZoneConditions[i]
            val originalFile = originalVersion?.enforcementZoneConditions[i]
            assertThat(file)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields(
                "id",
                "versionId",
                "version",
              )
              .isEqualTo(originalFile)
          }
        }
      }

      @Test
      fun `It should clone mandatoryAttendanceConditions referencing new version`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(
            firstValue.versions.last().mandatoryAttendanceConditions.count(),
          ).isEqualTo(originalVersion?.mandatoryAttendanceConditions?.count())

          firstValue.versions.last().mandatoryAttendanceConditions.forEach {
            assertThat(it.versionId).isEqualTo(firstValue.versions.last().id)
            assertThat(it.versionId).isNotEqualTo(originalVersionId)
          }
          for (i in firstValue.versions.last().mandatoryAttendanceConditions.indices) {
            val file = firstValue.versions.last().mandatoryAttendanceConditions[i]
            val originalFile = originalVersion?.mandatoryAttendanceConditions[i]
            assertThat(file)
              .usingRecursiveComparison()
              .ignoringCollectionOrder()
              .ignoringFields(
                "id",
                "versionId",
                "version",
              )
              .isEqualTo(originalFile)
          }
        }
      }
    }

    @Nested
    @DisplayName("Create Version as amend original request")
    inner class CreateVersionAsRequest {
      @BeforeEach
      fun setup() {
        service.createVersion(order.id, order.username, RequestType.AMEND_ORIGINAL_REQUEST)
      }

      @Test
      fun `It should create an new order version with type Request`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.id).isEqualTo(order.id)
          assertThat(firstValue.versions.count()).isEqualTo(2)
          assertThat(firstValue.versions.last().id).isNotEqualTo(originalVersionId)
          assertThat(firstValue.versions.last().status).isEqualTo(OrderStatus.IN_PROGRESS)
          assertThat(firstValue.versions.last().type).isEqualTo(RequestType.AMEND_ORIGINAL_REQUEST)
          assertThat(firstValue.versions.last().username).isEqualTo(order.username)
          assertThat(firstValue.versions.last().versionId).isEqualTo(1)
        }
      }

      @Test
      fun `It should set previous version type to REJECTED`() {
        argumentCaptor<Order>().apply {
          verify(repo, times(1)).save(capture())
          assertThat(firstValue.id).isEqualTo(order.id)
          assertThat(firstValue.versions.first().type).isEqualTo(RequestType.REJECTED)
        }
      }
    }
  }

  @Nested
  @DisplayName("Get version information")
  inner class GetVersionInformation {

    @Test
    fun `Should return version information in the correct order`() {
      val fixedDate = OffsetDateTime.of(2024, 5, 5, 0, 0, 0, 0, ZoneOffset.UTC)
      val secondDate = OffsetDateTime.of(2025, 5, 5, 0, 0, 0, 0, ZoneOffset.UTC)

      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED).apply {
        versions[0].fmsResultDate = fixedDate
        versions.add(
          OrderVersion(
            id = UUID.randomUUID(),
            orderId = this.id,
            username = "mockUser",
            status = OrderStatus.SUBMITTED,
            dataDictionaryVersion = DataDictionaryVersion.DDV5,
            type = RequestType.VARIATION,
            fmsResultDate = secondDate,
          ),
        )
      }

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))

      val result = service.getVersionInformation(mockOrder.id)

      assertThat(result).hasSize(2).extracting("versionId", "fmsResultDate", "status")
        .containsExactly(
          tuple(mockOrder.versions[1].id, secondDate, mockOrder.versions[1].status),
          tuple(mockOrder.versions[0].id, fixedDate, mockOrder.versions[0].status),
        )
    }
  }

  @Nested
  @DisplayName("Get specific order version")
  inner class GetSpecificVersion {

    @Test
    fun `Should return the specific version`() {
      val versionId2 = UUID.randomUUID()
      val version2 = OrderVersion(
        id = versionId2,
        orderId = UUID.randomUUID(),
        username = "mockUser",
        status = OrderStatus.SUBMITTED,
        dataDictionaryVersion = DataDictionaryVersion.DDV5,
        type = RequestType.VARIATION,
      )
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED).apply {
        versions.add(version2)
      }

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))

      val result = service.getSpecificVersion(mockOrder.id, versionId2)

      assertThat(result).isNotNull()
      assertThat(result.versionId).isEqualTo(versionId2)
      assertThat(result.versions).hasSize(1).containsExactly(version2)
    }

    @Test
    fun `Should throw exception when order ID is not found`() {
      val nonExistentOrderId = UUID.randomUUID()

      whenever(repo.findById(nonExistentOrderId)).thenReturn(Optional.empty())

      assertThatThrownBy {
        service.getSpecificVersion(nonExistentOrderId, UUID.randomUUID())
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Order with id $nonExistentOrderId does not exist")
    }

    @Test
    fun `Should throw exception when version ID does not exist within the order`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
      val nonExistentVersionId = UUID.randomUUID()

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))

      assertThatThrownBy {
        service.getSpecificVersion(mockOrder.id, nonExistentVersionId)
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining("Version does not exist for orderId ${mockOrder.id} and versionId $nonExistentVersionId")
    }
  }

  @Nested
  @DisplayName("Get fms request payload")
  inner class GetFmsRequest {

    @Test
    fun `Should throw exception when getting fms device wearer payload for a version is in progress`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.IN_PROGRESS)

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
      assertThatThrownBy {
        service.getFmsDeviceWearerPayload(mockOrder.id, mockOrder.versions.first().id)
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("This order is not submitted")
    }

    @Test
    fun `Should return fms device wearer payload by fms service`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
      mockOrder.fmsResultId = UUID.randomUUID()

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
      whenever(fmsService.getFmsDeviceWearerSubmissionResultById(mockOrder.fmsResultId!!)).thenReturn("mockPayload")

      val result = service.getFmsDeviceWearerPayload(mockOrder.id, mockOrder.versions.first().id)

      assertThat(result).isEqualTo("mockPayload")
    }

    @Test
    fun `Should throw exception when getting fms monitoring order payload for a version is in progress`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.IN_PROGRESS)

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
      assertThatThrownBy {
        service.getFmsMonitoringOrderPayload(mockOrder.id, mockOrder.versions.first().id)
      }
        .isInstanceOf(BadRequestException::class.java)
        .hasMessageContaining("This order is not submitted")
    }

    @Test
    fun `Should return fms monitoring order payload by fms service`() {
      val mockOrder = TestUtilities.createReadyToSubmitOrder(status = OrderStatus.SUBMITTED)
      mockOrder.fmsResultId = UUID.randomUUID()

      whenever(repo.findById(mockOrder.id)).thenReturn(Optional.of(mockOrder))
      whenever(
        fmsService.getFmsMonitoringOrderSubmissionResultByOrderId(mockOrder.fmsResultId!!),
      ).thenReturn("mockPayload")

      val result = service.getFmsMonitoringOrderPayload(mockOrder.id, mockOrder.versions.first().id)

      assertThat(result).isEqualTo("mockPayload")
    }
  }
}
