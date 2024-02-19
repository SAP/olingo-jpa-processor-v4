package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum ExampleProperties implements PropertyAccess {
  DESCRIPTION("Description"),
  EXTERNAL_VALUE("ExternalValue");

  private final String property;

  ExampleProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
