package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class FmsAttachmentResponse(
  val result: FmsAttachmentResult,
  val status: String? = null,
)

//TODO: do we need to manually map between snake_case and camelCase
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class FmsAttachmentResult(
  val sizeBytes: String? = null,
  val fileName: String? = null,
  val sysModCount: String? = null,
  val averageImageColor: String? = null,
  val imageWidth: String? = null,
  val sysUpdatedOn: String? = null,
  val sysTags: String? = null,
  val createdByName: String? = null,
  val sysId: String? = null,
  val updatedByName: String? = null,
  val imageHeight: String? = null,
  val sysUpdatedBy: String? = null,
  val downloadLink: String? = null,
  val contentType: String? = null,
  val sysCreatedOn: String? = null,
  val sizeCompressed: String? = null,
  val compressed: String? = null,
  val state: String? = null,
  val tableSysId: String? = null,
  val chunkSizeBytes: String? = null,
  val hash: String? = null,
  val sysCreatedBy: String? = null,
)