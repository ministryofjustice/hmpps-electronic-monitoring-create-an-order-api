package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.util.unit.DataSize
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase

class ConfigTest : IntegrationTestBase() {

  @Autowired
  private lateinit var environment: Environment

  @Test
  fun `should ensure file upload config is correct`() {
    val maxFileSize = environment.getProperty<DataSize>("spring.servlet.multipart.max-file-size")
    val maxRequestSize = environment.getProperty<DataSize>("spring.servlet.multipart.max-request-size")
    val httpCodecSize = environment.getProperty<DataSize>("spring.http.codecs.max-in-memory-size")

    val expectedSize = DataSize.ofMegabytes(36)

    assertThat(maxFileSize).isEqualTo(expectedSize)
    assertThat(maxRequestSize).isEqualTo(expectedSize)
    assertThat(httpCodecSize).isEqualTo(expectedSize)
  }

  @Test
  fun `should ensure jackson config is correct`() {
    val dateFormat = environment.getProperty<String>("spring.jackson.date-format")
    val includeDefaultView = environment.getProperty<Boolean>("spring.jackson.mapper.default-view-inclusion")

    assertThat(dateFormat).isEqualTo("yyyy-MM-dd HH:mm:ss")
    assertThat(includeDefaultView).isTrue()
  }
}
