package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums

enum class BailOrRemandToCareCondition(val id: String) {
  // Adult remittal for sentence on conditional bail (REMCB)
  REMCB("f917ba0c-1faf-4945-83a8-50be9049f9b4"),

  // Committed to Crown Court for sentence - in custody with bail direction (CCSIB)
  CCSIB("35430208-3705-44ce-b5d5-153c0337f6ab"),

  // Remand in care of Local Authority with bail direction (RILAB)
  RILAB("f666fd58-36c5-493f-aa11-89714faee6e6"),

  // Remanded in custody with bail direction (RIB)
  RIB("e26940b7-2534-42f2-9c44-c70072bf6ad2"),

  // Remanded on conditional bail (RC)
  RC("3a529001-2f43-45ba-a0a8-d3ced7e9e7ad"),

  // Remitted from the Crown Court to the Magistrates' Court in local authority accommodation with bail direction (RCCLAB)
  RCCLAB("9fd1849f-f91f-4fa7-adfd-ef24f64654eb"),

  // Sent to Crown Court in custody for trial with bail direction (CCIIB)
  CCIIB("062373fb-ada8-49a1-b7de-659426ba6b88"),

  // Sent to Crown Court for trial on conditional bail (CCIC)
  CCIC("b318ca35-8b6a-41e5-a674-879ac9a05cc2"),

  // Remitted from the Crown Court to the Magistrates' Court on conditional bail (RCCCB)
  RCCCB("6266e4d8-a030-4ee7-be5c-9f5624f162e5"),

  // Committed to Crown Court for sentence in Local Authority Accommodation (CCSILA)
  CCSILA("61dc2dfb-df0a-4ea3-8821-4506cb51e7ec"),

  // Youth remittal conditional bail (REMCBY)
  REMCBY("0536dbd2-b922-4899-9bc9-cad08429a889"),

  // Remand In care of Local Authority (RILA)
  RILA("903b3e90-f185-40d3-92dd-6f81b73c4bb2"),

  // Remitted from the Crown Court to the Magistrates' Court in local authority accommodation (RCCLA)
  RCCLA("975f04f2-412b-40f4-8e9b-31edfefaea60"),

  // Sent to Crown Court for trial in Local Authority Accommodation (CCIILA)
  CCIILA("d4fa6715-b1b3-4145-bedc-061ccf33df50"),

  // Youth remittal In Local Authority accommodation (REMIL)
  REMIL("e85dc2ee-9b63-4dca-8dc0-7dedaa00f5bf"),
  ;

  companion object {
    fun contains(id: String?) = BailOrRemandToCareCondition.entries.any { it.id == id }
  }
}
