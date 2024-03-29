package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum FilterRestrictionsProperties implements PropertyAccess {

  FILTERABLE("Filterable"),
  REQUIRES_FILTER("RequiresFilter"),
  REQUIRED_PROPERTIES("RequiredProperties");

  private final String property;

  private FilterRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
