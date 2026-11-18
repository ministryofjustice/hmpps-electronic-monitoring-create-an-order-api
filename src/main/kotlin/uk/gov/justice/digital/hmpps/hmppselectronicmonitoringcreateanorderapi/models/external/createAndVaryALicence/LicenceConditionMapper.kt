package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.createAndVaryALicence

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.interfaces.MonitoringCondition
import java.time.ZoneId
import java.util.*

enum class CvlConditionCode(val code: String) {
  CURFEW("0a370862-5426-49c1-b6d4-3d074d78a81a"),
  TRAIL("524f2fd6-ad53-47dd-8edc-2161d3dd2ed4"),
  ALCOHOL_LEVEL("2F8A5418-C6E4-4F32-9E58-64B23550E504"),
  ALCOHOL_ABSTINENCE("d36a3b77-30ba-40ce-8953-83e761d3b487"),
  RESTRICTION("005d70e4-a247-4f82-b8b3-6d294a0f5051"),
  EXCLUSION_ATTACHMENT("0f9a20f4-35c7-4c77-8af8-f200f153fa11"),
  EXCLUSION_TEXT("42f71b40-84cd-446d-8647-f00bbb6c079c"),
  ;

  companion object {
    fun from(code: String?) = entries.firstOrNull { it.code.equals(code, true) }
  }
}

@Component
class LicenceConditionMapper {
  private companion object {
    const val ELECTRONIC_MONITORING_RESTRICTIONS_CODE = "fd129172-bdd3-4d97-a4a0-efd7b47a49d4"
  }

  fun map(licence: Licence, versionId: UUID): List<MonitoringCondition> {
    val additionalConditions = licence.conditions.ap?.additional.orEmpty()
    val result = mutableListOf<MonitoringCondition>()
    val electronicMonitoringParentCondition = additionalConditions
      .firstOrNull { it.code.equals(ELECTRONIC_MONITORING_RESTRICTIONS_CODE, true) }
    val electronicMonitoringRestrictions = electronicMonitoringParentCondition?.restrictions
    val electronicMonitoringDefaultEndDate = electronicMonitoringParentCondition?.endDate?.toInstant()?.atZone(
      ZoneId.systemDefault(),
    )

    // TODO: Do AP additional for now, need to confirm on if others are even required...
    for (item in additionalConditions) {
      val startDate = licence.licenceStartDate.toInstant().atZone(ZoneId.systemDefault())
      val endDate = item.endDate?.toInstant()?.atZone(ZoneId.systemDefault())
      when (CvlConditionCode.from(item.code)) {
        CvlConditionCode.CURFEW -> result.add(
          CurfewConditions(
            versionId = versionId,
            startDate = startDate,
            endDate = electronicMonitoringDefaultEndDate,
            curfewDescription = item.text,
          ),
        )
        CvlConditionCode.TRAIL -> result.add(
          TrailMonitoringConditions(
            versionId = versionId,
            startDate = startDate,
            endDate = endDate,
          ),
        )
        CvlConditionCode.ALCOHOL_LEVEL -> result.add(
          AlcoholMonitoringConditions(
            versionId = versionId,
            startDate = startDate,
            endDate = endDate,
            monitoringType = AlcoholMonitoringType.ALCOHOL_LEVEL,
          ),
        )
        CvlConditionCode.ALCOHOL_ABSTINENCE -> result.add(
          AlcoholMonitoringConditions(
            versionId = versionId,
            startDate = startDate,
            endDate = endDate,
            monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
          ),
        )
        CvlConditionCode.RESTRICTION -> {
          if (electronicMonitoringRestrictions?.contains("RESTRICTION_ZONE") ?: false) {
            result.add(
              EnforcementZoneConditions(
                versionId = versionId,
                startDate = startDate,
                endDate = electronicMonitoringDefaultEndDate,
                zoneType = EnforcementZoneType.RESTRICTION,
                description = item.text,
              ),
            )
          }
        }
        CvlConditionCode.EXCLUSION_ATTACHMENT, CvlConditionCode.EXCLUSION_TEXT -> {
          if (electronicMonitoringRestrictions?.contains("EXCLUSION_ZONE") ?: false) {
            result.add(
              EnforcementZoneConditions(
                versionId = versionId,
                startDate = startDate,
                endDate = electronicMonitoringDefaultEndDate,
                zoneType = EnforcementZoneType.EXCLUSION,
                description = item.text,
              ),
            )
          }
        }
        null -> continue // For now just doing nothing if it cant be mapped
      }
    }
    return result
  }
}
