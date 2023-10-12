package com.sap.olingo.jpa.processor.core.util;

import jakarta.persistence.TupleElement;

public class TupleElementDouble implements TupleElement<Object> {
  // alias
  private final String alias;
  private final Object value;

  public TupleElementDouble(final String alias, final Object value) {
    super();
    this.alias = alias;
    this.value = value;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public Class<? extends Object> getJavaType() {
    return value.getClass();
  }

}
