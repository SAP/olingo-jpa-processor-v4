package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.server.api.ODataApplicationException;

class JPAAggregationOperation implements JPAOperator {

  private final Root<?> root;
  private final JPAOperationConverter converter;

  public JPAAggregationOperation(Root<?> root, JPAOperationConverter converter) {
    this.root = root;
    this.converter = converter;
  }

  @Override
  public Object get() throws ODataApplicationException {
    return converter.convert(this);
  }

  JPAFilterAggregationType getAggregation() {
    return JPAFilterAggregationType.COUNT;
  }

  Expression<?> getPath() {
    return root; // keyPathList;
  }

}
