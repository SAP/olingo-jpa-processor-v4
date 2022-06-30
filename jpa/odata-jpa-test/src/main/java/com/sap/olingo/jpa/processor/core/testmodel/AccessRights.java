package com.sap.olingo.jpa.processor.core.testmodel;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(isFlags = true, converter = AccessRightsConverter.class)
public enum AccessRights {
  READ((short) 1), WRITE((short) 2), CREATE((short) 4), DELETE((short) 8);

  private short value;

  private AccessRights(short value) {
    this.setValue(value);
  }

  public short getValue() {
    return value;
  }

  private void setValue(short value) {
    this.value = value;
  }
}
