package com.sap.olingo.jpa.processor.core.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(isFlags = true, converter = FileAccessConverter.class)
public enum FileAccess {
  Read((short) 1), Write((short) 2), Create((short) 4), Delete((short) 8);

  private final short value;

  private FileAccess(final short value) {
    this.value = value;
  }

  public short getValue() {
    return value;
  }
}

