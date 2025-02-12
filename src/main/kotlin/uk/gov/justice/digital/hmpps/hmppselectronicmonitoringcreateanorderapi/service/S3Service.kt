package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.HmppsS3Properties
import java.nio.charset.StandardCharsets

@Service
@EnableConfigurationProperties(
  HmppsS3Properties::class,
)
class S3Service(
  private val s3Client: S3Client,
  private val hmppsS3Properties: HmppsS3Properties,
) {

  fun getObject(key: String): String {
    val request = GetObjectRequest.builder()
      .bucket(hmppsS3Properties.bucketName)
      .key(key)
      .build()
    return s3Client.getObjectAsBytes(request).asString(StandardCharsets.UTF_8)
  }
}
