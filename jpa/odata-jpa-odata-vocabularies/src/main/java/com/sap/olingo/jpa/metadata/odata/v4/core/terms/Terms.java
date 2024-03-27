package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.TermAccess;

public enum Terms implements TermAccess {

  COMPUTED("Computed"),
  COMPUTED_DEFAULT_VALUE("ComputedDefaultValue"),
  IMMUTABLE("Immutable"),
  EXAMPLE("Example");

  private final String term;

  Terms(final String term) {
    this.term = term;
  }

  @Override
  public String term() {
    return term;
  }
}
