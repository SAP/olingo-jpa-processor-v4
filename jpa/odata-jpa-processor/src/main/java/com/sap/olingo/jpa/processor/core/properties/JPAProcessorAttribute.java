package com.sap.olingo.jpa.processor.core.properties;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public interface JPAProcessorAttribute {
  /**
   *
   * @return
   */
  @Nonnull
  String getAlias();

  /**
   * As of now transient properties are not sortable
   * @return
   */
  boolean isSortable();

  /**
   * @return True if sorting descending is required
   */
  boolean sortDescending();

  /**
   * Indicates that a join is required to fulfill the request
   * @return
   */
  boolean requiresJoin();

  /**
   *
   * @param target
   * @param joinTables
   * @param cb
   * @return
   */
  JPAProcessorAttribute setTarget(@Nonnull final From<?, ?> target, @Nonnull final Map<String, From<?, ?>> joinTables,
      @Nonnull final CriteriaBuilder cb);

  /**
   * If required, a join with 'from' is generated. If a join is required can be checked with
   * {@link #requiresJoin()}.<br>
   * Requires that the target was already set via
   * @param <T>
   * @param <S>
   * @return Null if no join is required
   */
  <T, S> Join<T, S> createJoin();

  /**
   * Generates an order statement for this attribute.<br>
   * Requires that {@link #createJoin(From, CriteriaBuilder)} was called before.
   * @param cb
   * @param groups
   * @return
   * @throws ODataJPAQueryException
   */
  @Nonnull
  Order createOrderBy(final CriteriaBuilder cb, final List<String> groups) throws ODataJPAQueryException;

  /**
   * Requires that {@link #setTarget(From, CriteriaBuilder)} was called before.
   * @return
   */
  Path<Object> getPath();

}
