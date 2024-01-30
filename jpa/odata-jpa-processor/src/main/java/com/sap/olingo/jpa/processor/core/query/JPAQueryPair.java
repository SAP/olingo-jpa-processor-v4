package com.sap.olingo.jpa.processor.core.query;

record JPAQueryPair(JPAAbstractQuery inner, JPAAbstractQuery outer) {

  @Override
  public String toString() {
    return "JPAQueryPair [outer=" + outer + ", inner=" + inner + "]";
  }

}
