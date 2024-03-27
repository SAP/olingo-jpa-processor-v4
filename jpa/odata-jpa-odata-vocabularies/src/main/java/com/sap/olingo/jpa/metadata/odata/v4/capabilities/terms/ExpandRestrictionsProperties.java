package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum ExpandRestrictionsProperties implements PropertyAccess {

  EXPANDABLE("Expandable"),
  NON_EXPANDABLE_PROPERTIES("NonExpandableProperties"),
  MAX_LEVELS("MaxLevels");

  private final String property;

  private ExpandRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
