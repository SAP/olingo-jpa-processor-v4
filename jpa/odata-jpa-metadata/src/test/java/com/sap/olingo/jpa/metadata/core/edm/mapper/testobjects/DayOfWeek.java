package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.ODataEnum;

public enum DayOfWeek implements ODataEnum {
  MONDAY(1), TUESDAY(2), WEDNESDAY(3),
  THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);

  private final int value;

  DayOfWeek(int ordinal) {
    value = ordinal;
  }

  @Override
  public Integer getValue() {
    return value;
  }
}
