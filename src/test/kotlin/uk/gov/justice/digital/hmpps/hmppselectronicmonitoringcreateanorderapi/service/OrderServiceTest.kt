package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAppointment
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationLocation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderListCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.criteria.OrderSearchCriteria
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
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

    Assertions.assertThat(result.id).isNotNull()
    Assertions.assertThat(UUID.fromString(result.id.toString())).isEqualTo(result.id)
    Assertions.assertThat(result.username).isEqualTo("mockUser")
    Assertions.assertThat(result.status).isEqualTo(OrderStatus.IN_PROGRESS)
    Assertions.assertThat(result.dataDictionaryVersion).isEqualTo(DataDictionaryVersion.DDV4)
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
      Assertions.assertThat(firstValue.getCurrentVersion().submittedBy).isEqualTo("mockName")
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
      Assertions.assertThat(firstValue.fmsResultId).isEqualTo(mockFmsResult.id)
    }
  }

  @Test
  fun `Should be able to list all orders`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(startDate = mockStartDate, endDate = mockEndDate)
    val mockCriteria = OrderListCriteria(username = "test")

    whenever(repo.findAll(ArgumentMatchers.any(OrderListSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.listOrders(mockCriteria)

    Assertions.assertThat(result).isEqualTo(listOf(mockOrder))
  }

  @Test
  fun `Should be able to search for orders`() {
    val mockOrder = TestUtilities.createReadyToSubmitOrder(startDate = mockStartDate, endDate = mockEndDate)
    val mockCriteria = OrderSearchCriteria(searchTerm = "Bob Smith")

    whenever(repo.findAll(ArgumentMatchers.any(OrderSearchSpecification::class.java))).thenReturn(listOf(mockOrder))

    val result = service.searchOrders(mockCriteria)

    Assertions.assertThat(result).isEqualTo(listOf(mockOrder))
  }

  @Nested
  @DisplayName("Create Version")
  inner class CreateVersion {
    val originalVersionId: UUID = UUID.randomUUID()
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
        ),
        AdditionalDocument(
          versionId = originalVersionId,
          fileName = "MockPhotoFile",
          fileType = DocumentType.PHOTO_ID,
        ),
      ),
    )
    val originalVersion = order.versions.find { it.id == originalVersionId }

    @BeforeEach
    fun setup() {
      service = OrderService(repo, fmsService, "DDV5")
      whenever(repo.findById(order.id)).thenReturn(Optional.of(order))
      whenever(repo.save(order)).thenReturn(order)

      service.createVersion(order.id, order.username)
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
    fun `It should clone interestedParties referencing new version`() {
      argumentCaptor<Order>().apply {
        verify(repo, times(1)).save(capture())
        assertThat(firstValue.versions.last().interestedParties).isNotNull()
        assertThat(firstValue.versions.last().interestedParties?.versionId).isEqualTo(firstValue.versions.last().id)
        assertThat(firstValue.versions.last().interestedParties?.versionId).isNotEqualTo(originalVersionId)
        assertThat(firstValue.versions.last().interestedParties)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .ignoringFields(
            "id",
            "versionId",
            "version",
          )
          .isEqualTo(originalVersion?.interestedParties)
      }
    }

    @Test
    fun `It should clone probationDeliveryUnit referencing new version`() {
      argumentCaptor<Order>().apply {
        verify(repo, times(1)).save(capture())
        assertThat(firstValue.versions.last().probationDeliveryUnit).isNotNull()
        assertThat(firstValue.versions.last().probationDeliveryUnit?.versionId).isEqualTo(firstValue.versions.last().id)
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
        assertThat(firstValue.versions.last().monitoringConditions?.versionId).isEqualTo(firstValue.versions.last().id)
        assertThat(firstValue.versions.last().monitoringConditions?.versionId).isNotEqualTo(originalVersionId)
        assertThat(firstValue.versions.last().monitoringConditions)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .ignoringFields(
            "id",
            "versionId",
            "version",
          )
          .isEqualTo(originalVersion?.monitoringConditions)
      }
    }

    @Test
    fun `It should clone installationLocation referencing new version`() {
      argumentCaptor<Order>().apply {
        verify(repo, times(1)).save(capture())
        assertThat(firstValue.versions.last().installationLocation?.versionId).isEqualTo(firstValue.versions.last().id)
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
}
