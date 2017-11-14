package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(isFlags = true, converter = WrongMemberConverter.class)
public enum WrongMember {
  Right(1), Wrong(-2);
  private final int value;

  private WrongMember(final int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
