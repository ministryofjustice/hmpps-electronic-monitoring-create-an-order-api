package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders

@OpenAPIDefinition(
  info = io.swagger.v3.oas.annotations.info.Info(
    title = "HMPPS Create Electronic Monitoring Order API Documentation",
    description = "A service for create, maintain and submit Electronic Monitoring Order.",
    license = io.swagger.v3.oas.annotations.info.License(
      name = "The MIT License (MIT)",
      url = "https://github.com/ministryofjustice/court-case-service/blob/main/LICENSE",
    ),
    version = "1.0",
  ),
  security = [SecurityRequirement(name = "hmpps-auth-token")],
)
@SecurityScheme(
  name = "hmpps-auth-token",
  scheme = "bearer",
  bearerFormat = "JWT",
  type = SecuritySchemeType.HTTP,
  `in` = SecuritySchemeIn.HEADER,
  paramName = HttpHeaders.AUTHORIZATION,
)
@Configuration
class OpenApiConfiguration
