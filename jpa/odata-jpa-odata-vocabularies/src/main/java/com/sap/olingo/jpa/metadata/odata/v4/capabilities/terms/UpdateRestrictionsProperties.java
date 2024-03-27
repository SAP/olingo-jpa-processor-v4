package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum UpdateRestrictionsProperties implements PropertyAccess {

  UPDATEABLE("Updatable"),
  UPSERTABLE("Upsertable"),
  UPDATE_METHOD("UpdateMethod"),
  NON_UPDATEABLE_PROPERTIES("NonUpdatableProperties"),
  NON_UPDATEABLE_NAVIGATION_PROPERTIES("NonUpdatableNavigationProperties"),
  REQUIRED_PROPERTIES("RequiredProperties"),
  MAX_LEVELS("MaxLevels"),
  DESCRIPTION("Description"),
  LONG_DESCRIPTION("LongDescription");

  private final String property;

  private UpdateRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
