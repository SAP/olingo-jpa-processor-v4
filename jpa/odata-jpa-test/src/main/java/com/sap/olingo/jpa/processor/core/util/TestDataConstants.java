package com.sap.olingo.jpa.processor.core.util;

public enum TestDataConstants {

  NO_ATTRIBUTES_POSTAL_ADDRESS(10),
  NO_ATTRIBUTES_POSTAL_ADDRESS_T(1),
  NO_ATTRIBUTES_COMMUNICATION_DATA(4),
  NO_ATTRIBUTES_CHANGE_INFO(2),
  NO_ATTRIBUTES_BUSINESS_PARTNER(6),
  NO_ATTRIBUTES_BUSINESS_PARTNER_T(1),
  NO_ATTRIBUTES_ORGANIZATION(4),
  NO_ATTRIBUTES_PERSON(2),
  NO_DEC_ATTRIBUTES_BUSINESS_PARTNER(9),
  NO_ENTITY_TYPES(32),
  NO_ENTITY_SETS(31),
  NO_SINGLETONS(2);

  public final int value;

  TestDataConstants(final int i) {
    value = i;
  }

}
