package com.sap.olingo.jpa.processor.cb;

import java.util.List;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

public interface ProcessorCriteriaBuilder extends CriteriaBuilder {

  @Override
  public ProcessorCriteriaQuery<Tuple> createTupleQuery();

  @Override
  public ProcessorCriteriaQuery<Object> createQuery();

  @Override
  public <T> ProcessorCriteriaQuery<T> createQuery(final Class<T> resultClass);

  public WindowFunction<Long> rowNumber();

  /**
   * Create predicate to test whether given expression
   * is contained in a list of values.
   * @param list of path to be tested against list of values
   * @return in predicate
   */
  public In<List<Comparable<?>>> in(final List<Path<Comparable<?>>> expression,
      final Subquery<List<Comparable<?>>> subquery);

  /**
   * Create predicate to test whether given expression
   * is contained in a list of values.
   * @param path to be tested against list of values
   * @return in predicate
   */
  public <T> In<T> in(final Path<?> path);

  public default void resetParameterBuffer() {}

  public default Object getParameterBuffer() {
    return null;
  }

  public static interface WindowFunction<T> extends Expression<T> {
    /**
     *
     * @param order
     * @return
     */
    WindowFunction<T> orderBy(final Order... order);

    WindowFunction<T> orderBy(final List<Order> order);

    /**
     * Takes an array of simple path expressions.
     * @param path
     * @return
     */
    WindowFunction<T> partitionBy(@SuppressWarnings("unchecked") final Path<Comparable<?>>... path);

    WindowFunction<T> partitionBy(final List<Path<Comparable<?>>> path);

    Path<T> asPath(final String tableAlias);

  }
}
