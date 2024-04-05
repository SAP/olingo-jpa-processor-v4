package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;

public enum SortRestrictionsProperties implements PropertyAccess {

  SORTABLE("Sortable"),
  ASCENDING_ONLY_PROPERTIES("AscendingOnlyProperties"),
  DESCENDING_ONLE_PROPERTIES("DescendingOnlyProperties"),
  NON_SORTABLE_PROPERTIES("NonSortableProperties");

  private final String property;

  private SortRestrictionsProperties(final String property) {
    this.property = property;
  }

  @Override
  public String property() {
    return property;
  }

}
