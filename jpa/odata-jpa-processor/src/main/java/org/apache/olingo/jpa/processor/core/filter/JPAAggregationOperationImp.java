package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.server.api.ODataApplicationException;

class JPAAggregationOperationImp implements JPAAggregationOperation {

  private final Root<?> root;
  private final JPAOperationConverter converter;

  public JPAAggregationOperationImp(final Root<?> root, final JPAOperationConverter converter) {
    this.root = root;
    this.converter = converter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAAggregationOperation#get()
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
    return root; // keyPathList;
  }

}
