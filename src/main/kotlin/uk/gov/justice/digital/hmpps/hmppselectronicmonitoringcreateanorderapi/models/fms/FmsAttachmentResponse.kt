package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class FmsAttachmentResponse(
  val result: FmsAttachmentResult,
  val status: String? = null,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class FmsAttachmentResult(
  val sizeBytes: String = "",
  val fileName: String = "",
  val sysModCount: String = "",
  val averageImageColor: String = "",
  val imageWidth: String = "",
  val sysUpdatedOn: String = "",
  val sysTags: String = "",
  val createdByName: String = "",
  val tableName: String = "",
  val sysId: String = "",
  val updatedByName: String = "",
  val imageHeight: String = "",
  val sysUpdatedBy: String = "",
  val downloadLink: String = "",
  val contentType: String = "",
  val sysCreatedOn: String = "",
  val sizeCompressed: String = "",
  val compressed: String = "",
  val state: String = "",
  val tableSysId: String = "",
  val chunkSizeBytes: String = "",
  val hash: String = "",
  val sysCreatedBy: String = "",
)
