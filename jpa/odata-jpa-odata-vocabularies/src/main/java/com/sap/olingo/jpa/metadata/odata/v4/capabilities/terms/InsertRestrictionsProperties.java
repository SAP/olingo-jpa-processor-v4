package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum InsertRestrictionsProperties implements PropertyAccess {

  INSERTABLE("Insertable"),
  NON_INSERTABLE_PROPERTIES("NonInsertableProperties"),
  NON_INSERTABLE_NAVIGATION_PROPERTIES("NonInsertableNavigationProperties"),
  REQUIRED_PROPERTIES("RequiredProperties"),
  MAX_LEVELS("MaxLevels"),
  DESCRIPTION("Description"),
  LONG_DESCRIPTION("LongDescription");

  private final String property;

  private InsertRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
