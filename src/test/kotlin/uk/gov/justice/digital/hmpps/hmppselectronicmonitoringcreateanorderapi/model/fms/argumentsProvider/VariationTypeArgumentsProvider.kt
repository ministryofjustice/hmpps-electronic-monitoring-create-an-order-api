package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.model.fms.argumentsProvider

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import java.util.stream.Stream

class VariationTypeArgumentsProvider : ArgumentsProvider {
  override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? = Stream.of(
    Arguments.of(VariationType.CHANGE_TO_DEVICE_TYPE, "Change to Device Type"),
    Arguments.of(VariationType.ADMIN_ERROR, "Admin Error"),
    Arguments.of(VariationType.OTHER, "Other"),
    Arguments.of(VariationType.CHANGE_TO_ADDRESS, "Change to Address"),
    Arguments.of(VariationType.CHANGE_TO_PERSONAL_DETAILS, "Change to Personal Details"),
    Arguments.of(VariationType.CHANGE_TO_ADD_AN_EXCLUSION_ZONES, "Change to add an Inclusion or Exclusion Zone(s)"),
    Arguments.of(VariationType.CHANGE_TO_AN_EXISTING_EXCLUSION, "Change to an existing Inclusion or Exclusion Zone(s)"),
    Arguments.of(VariationType.CHANGE_TO_CURFEW_HOURS, "Change to Curfew Hours"),
    Arguments.of(VariationType.ORDER_SUSPENSION, "Order Suspension"),
    Arguments.of(VariationType.CHANGE_TO_ENFORCEABLE_CONDITION, "Change to Enforceable Condition"),
  )
}
