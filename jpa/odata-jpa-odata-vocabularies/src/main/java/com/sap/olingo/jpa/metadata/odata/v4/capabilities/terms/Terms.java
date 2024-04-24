package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.TermAccess;

public enum Terms implements TermAccess {

  COUNT_RESTRICTIONS("CountRestrictions"),
  DEEP_INSERT_SUPPORT("DeepInsertSupport"),
  DEEP_UPDATE_SUPPORT("DeepUpdateSupport"),
  DELETE_RESTRICTIONS("DeleteRestrictions"),
  EXPAND_RESTRICTIONS("ExpandRestrictions"),
  FILTER_RESTRICTIONS("FilterRestrictions"),
  INSERT_RESTRICTIONS("InsertRestrictions"),
  SORT_RESTRICTIONS("SortRestrictions"),
  UPDATE_RESTRICTIONS("UpdateRestrictions");

  private final String term;

  Terms(final String term) {
    this.term = term;
  }

  @Override
  public String term() {
    return term;
  }
}
