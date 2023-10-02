package com.sap.olingo.jpa.processor.cb;

import java.util.List;

import javax.annotation.Nullable;

import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

public interface ProcessorSubquery<T> extends Subquery<T>, ProcessorSubQueryProvider {

  @Override
  public <U> ProcessorSubquery<U> subquery(Class<U> type);

  /**
   * Specify the selection items that are to be returned in the
   * query result.<br>
   * Replaces the previously specified selection(s), if any. Alias are ignored.
   * <p>
   * Main purpose is to use multiselect together with the IN operator.
   * @param selections selection items corresponding to the
   * results to be returned by the query
   * @return the modified query
   * @throws IllegalArgumentException if the selection is
   * a compound selection and more than one selection
   * item has the same assigned alias
   */
  ProcessorSubquery<T> multiselect(Selection<?>... selections);

  /**
   * Specify the selection items that are to be returned in the
   * query result.<br>
   * Replaces the previously specified selection(s), if any. Alias are ignored.
   * <p>
   * Main purpose is to use multiselect together with the IN operator.
   * @param selectionList selection items corresponding to the
   * results to be returned by the query
   * @return the modified query
   * @throws IllegalArgumentException if the selection is
   * a compound selection and more than one selection
   * item has the same assigned alias
   */
  ProcessorSubquery<T> multiselect(List<Selection<?>> selectionList);

  /**
   * Specify the ordering expressions that are used to
   * order the query results.
   * Replaces the previous ordering expressions, if any.
   * If no ordering expressions are specified, the previous
   * ordering, if any, is simply removed, and results will
   * be returned in no particular order.
   * The order of the ordering expressions in the list
   * determines the precedence, whereby the first element in the
   * list has highest precedence.
   * @param o list of zero or more ordering expressions
   * @return the modified query
   */
  ProcessorSubquery<T> orderBy(final List<Order> o);

  /**
   * Specify the ordering expressions that are used to
   * order the query results.
   * Replaces the previous ordering expressions, if any.
   * If no ordering expressions are specified, the previous
   * ordering, if any, is simply removed, and results will
   * be returned in no particular order.
   * The left-to-right sequence of the ordering expressions
   * determines the precedence, whereby the leftmost has highest
   * precedence.
   * @param o zero or more ordering expressions
   * @return the modified query
   */
  ProcessorSubquery<T> orderBy(Order... o);

  /**
   * Set the maximum number of results to retrieve.
   * @param maxResult maximum number of results to retrieve
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ProcessorSubquery<T> setMaxResults(@Nullable final Integer maxResult);

  /**
   * Set the position of the first result to retrieve.
   * @param startPosition position of the first result,
   * numbered from 0
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ProcessorSubquery<T> setFirstResult(@Nullable final Integer startPosition);

  <X> Root<X> from(ProcessorSubquery<?> innerQuery);
}
