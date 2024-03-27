package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum DeleteRestrictionsProperties implements PropertyAccess {

  DELETABLE("Deletable"),
  NON_DELETABLE_NAVIGATION_PROPERTIES("NonDeletableNavigationProperties"),
  MAX_LEVELS("MaxLevels"),
  DESCRIPTION("Description"),
  LONG_DESCRIPTION("LongDescription");

  private final String property;

  private DeleteRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
