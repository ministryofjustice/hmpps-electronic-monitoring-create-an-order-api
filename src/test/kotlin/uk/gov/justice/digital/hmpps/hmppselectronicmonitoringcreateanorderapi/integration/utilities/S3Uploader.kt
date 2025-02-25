package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.HmppsS3Properties

@Service
@EnableConfigurationProperties(
  HmppsS3Properties::class,
)
class S3Uploader(private val s3Client: S3Client, private val hmppsS3Properties: HmppsS3Properties) {
  fun createBucket() {
    try {
      val headBucketRequest = HeadBucketRequest.builder()
        .bucket(hmppsS3Properties.bucketName)
        .build()

      s3Client.headBucket(headBucketRequest)
    } catch (e: NoSuchBucketException) {
      val bucketRequest = CreateBucketRequest.builder()
        .bucket(hmppsS3Properties.bucketName)
        .build()

      s3Client.createBucket(bucketRequest)
    }
  }

  fun uploadObject(value: String, key: String): String {
    val request = PutObjectRequest.builder()
      .bucket(hmppsS3Properties.bucketName)
      .key(key)
      .build()
    val requestBody = RequestBody.fromString(value)
    return s3Client.putObject(request, requestBody).eTag()
  }
}
