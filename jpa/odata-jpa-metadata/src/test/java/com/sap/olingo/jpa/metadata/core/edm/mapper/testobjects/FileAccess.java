package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(isFlags = true, converter = FileAccessConverter.class)
public enum FileAccess {
  Read((short) 1), Write((short) 2), Create((short) 4), Delete((short) 8);

  private short value;

  private FileAccess(short value) {
    this.setValue(value);
  }

  public short getValue() {
    return value;
  }

  private void setValue(short value) {
    this.value = value;
  }
}
