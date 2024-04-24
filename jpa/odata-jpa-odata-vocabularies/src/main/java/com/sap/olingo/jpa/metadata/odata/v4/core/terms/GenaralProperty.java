package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum GenaralProperty implements PropertyAccess {
  VALUE("Value");

  private final String property;

  GenaralProperty(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
