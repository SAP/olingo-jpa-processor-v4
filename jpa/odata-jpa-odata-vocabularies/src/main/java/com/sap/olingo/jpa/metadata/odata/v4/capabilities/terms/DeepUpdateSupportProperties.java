package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum DeepUpdateSupportProperties implements PropertyAccess {

  SUPPORTED("supported"),
  CONTENT_ID_SUPPORTED("contentIDSupported");

  private final String property;

  private DeepUpdateSupportProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
