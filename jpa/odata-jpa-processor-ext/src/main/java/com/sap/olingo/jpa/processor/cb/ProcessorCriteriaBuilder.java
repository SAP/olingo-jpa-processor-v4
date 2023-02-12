package com.sap.olingo.jpa.processor.cb;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

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
  public <T> In<T> in(final List<Path<? extends T>> expression, final Subquery<?> subquery);

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
    WindowFunction<T> partitionBy(final Path<?>... path);
    
    WindowFunction<T> partitionBy(final List<Path<?>> path);

    Path<T> asPath(final String tableAlias);

  }
}
