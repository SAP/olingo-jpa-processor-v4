package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.api.CardinalityValue.MANY;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.api.CardinalityValue.ONE;

public enum Cardinality {

  MANY_TO_ONE(MANY, ONE),
  ONE_TO_ONE(ONE, ONE),
  MANY_TO_MANY(MANY, MANY),
  ONE_TO_MANY(ONE, MANY);

  public final CardinalityValue source;
  public final CardinalityValue target;

  private Cardinality(final CardinalityValue source, final CardinalityValue target) {
    this.source = source;
    this.target = target;
  }
}
