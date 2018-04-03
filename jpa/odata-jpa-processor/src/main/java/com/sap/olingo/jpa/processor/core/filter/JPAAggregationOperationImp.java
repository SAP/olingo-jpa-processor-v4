package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

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

  Expression<?> getPath() {
    return root;
  }

}
