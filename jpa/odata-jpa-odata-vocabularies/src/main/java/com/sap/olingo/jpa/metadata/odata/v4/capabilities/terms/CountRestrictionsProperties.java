package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum CountRestrictionsProperties implements PropertyAccess {

  COUNTABLE("Countable"),
  NON_COUNTABLE_PROPERTIES("NonCountableProperties"),
  NON_COUNTABLE_NAVIGATION_PROPERTIES("NonCountableNavigationProperties");

  private final String property;

  private CountRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
