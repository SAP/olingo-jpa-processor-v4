package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.math.BigDecimal;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(converter = WrongTypeConverter.class)
public enum WrongType {
  TEST(BigDecimal.valueOf(2L));

  private BigDecimal value;

  private WrongType(BigDecimal value) {
    this.value = value;
  }

  public BigDecimal getValue() {
    return value;
  }
}
