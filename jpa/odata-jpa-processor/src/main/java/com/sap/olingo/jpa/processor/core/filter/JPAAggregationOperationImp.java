package com.sap.olingo.jpa.processor.core.filter;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import org.apache.olingo.server.api.ODataApplicationException;

class JPAAggregationOperationImp implements JPAAggregationOperation {

  private final From<?, ?> root;
  private final JPAOperationConverter converter;

  public JPAAggregationOperationImp(final From<?, ?> root, final JPAOperationConverter converter) {
    this.root = root;
    this.converter = converter;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPAAggregationOperation#get()
   */
  @Override
  public Object get() throws ODataApplicationException {
    return converter.convert(this);
  }

  @Override
  public JPAFilterAggregationType getAggregation() {
    return JPAFilterAggregationType.COUNT;
  }

  @SuppressWarnings("unchecked")
  <S> Expression<S> getPath() {
    return (Expression<S>) root;
  }

  @Override
  public String getName() {
    return JPAFilterAggregationType.COUNT.name();
  }

}
