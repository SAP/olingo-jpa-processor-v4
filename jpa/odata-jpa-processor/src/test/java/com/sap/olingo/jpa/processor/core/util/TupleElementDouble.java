package com.sap.olingo.jpa.processor.core.util;

import javax.persistence.TupleElement;

public class TupleElementDouble implements TupleElement<Object> {
  // alias
  private final String alias;
  private final Object value;

  public TupleElementDouble(String alias, Object value) {
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
