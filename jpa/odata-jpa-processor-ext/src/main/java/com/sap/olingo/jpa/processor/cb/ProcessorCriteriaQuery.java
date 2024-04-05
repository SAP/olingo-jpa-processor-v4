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

  /**
   * The position of the first result the query object was set to
   * retrieve. Returns 0 if <code>setFirstResult</code> was not applied to the
   * query object.
   * @return position of the first result
   * @since 2.0
   */
  int getFirstResult();

  /**
   * Set the position of the first result to retrieve.
   * @param startPosition position of the first result,
   * numbered from 0
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  void setFirstResult(int startPosition);

  /**
   * The maximum number of results the query object was set to
   * retrieve. Returns <code>Integer.MAX_VALUE</code> if <code>setMaxResults</code> was not
   * applied to the query object.
   * @return maximum number of results
   * @since 2.0
   */
  int getMaxResults();

  /**
   * Set the maximum number of results to retrieve.
   * @param maxResult maximum number of results to retrieve
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  void setMaxResults(int maxResult);

}
