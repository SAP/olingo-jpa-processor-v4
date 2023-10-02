package com.sap.olingo.jpa.processor.cb;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public interface ProcessorCriteriaQuery<T> extends CriteriaQuery<T>, ProcessorSubQueryProvider {

  @Override
  public <U> ProcessorSubquery<U> subquery(Class<U> type);

  /**
   * Create and add a query root corresponding to the given entity,
   * forming a cartesian product with any existing roots.
   * @param subquery
   * @return query root corresponding to the given entity
   */
  <X> Root<X> from(final ProcessorSubquery<X> subquery);

}
