package com.sap.olingo.jpa.processor.cb.api;

public interface ProcessorSubquery<T> extends javax.persistence.criteria.Subquery<T> {
  /**
   * Set the maximum number of results to retrieve.
   * @param maxResult maximum number of results to retrieve
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ProcessorSubquery<T> setMaxResults(int maxResult);

  /**
   * Set the position of the first result to retrieve.
   * @param startPosition position of the first result,
   * numbered from 0
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ProcessorSubquery<T> setFirstResult(int startPosition);
}
