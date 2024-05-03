package com.sap.olingo.jpa.processor.cb.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

/**
 * The <code>Subquery</code> interface defines functionality that is
 * specific to subqueries.
 *
 * A subquery has an expression as its selection item.
 *
 * @author Oliver Grande
 *
 * @param <T> the type of the selection item.
 */
class SubqueryImpl<T> implements ProcessorSubquery<T>, SqlConvertible {
  private final Class<T> type;
  private final CriteriaQuery<?> parent;
  private final ProcessorCriteriaQuery<T> inner;
  private Optional<Integer> maxResult;
  private Optional<Integer> firstResult;

  SubqueryImpl(@Nonnull final Class<T> type, @Nonnull final CriteriaQuery<?> parent, final AliasBuilder ab,
      final CriteriaBuilder cb, final SqlPagingFunctions sqlPagingFunctions) {
    super();
    this.type = Objects.requireNonNull(type);
    this.parent = Objects.requireNonNull(parent);
    this.inner = new CriteriaQueryImpl<>(type, ((CriteriaQueryImpl<?>) parent).getServiceDocument(), ab, cb, sqlPagingFunctions);
    maxResult = Optional.empty();
    firstResult = Optional.empty();
  }

  @Override
  public Subquery<T> select(@Nonnull final Expression<T> expression) {
    inner.select(expression);
    return this;
  }

  @Override
  public Subquery<T> where(@Nonnull final Expression<Boolean> restriction) {
    inner.where(restriction);
    return this;
  }

  @Override
  public Subquery<T> where(@Nonnull final Predicate... restrictions) {
    inner.where(restrictions);
    return this;
  }

  @Override
  public Subquery<T> groupBy(@Nonnull final Expression<?>... grouping) {
    inner.groupBy(grouping);
    return this;
  }

  @Override
  public Subquery<T> groupBy(@Nonnull final List<Expression<?>> grouping) {
    inner.groupBy(grouping);
    return this;
  }

  @Override
  public Subquery<T> having(@Nonnull final Expression<Boolean> restriction) {
    inner.having(restriction);
    return this;
  }

  @Override
  public Subquery<T> having(@Nonnull final Predicate... restrictions) {
    inner.having(restrictions);
    return this;
  }

  @Override
  public Subquery<T> distinct(@Nonnull final boolean distinct) {
    inner.distinct(distinct);
    return this;
  }

  /**
   * Create a subquery root correlated to a root of the
   * enclosing query.
   * @param parentRoot a root of the containing query
   * @return subquery root
   */
  @Override
  public <Y> Root<Y> correlate(@Nonnull final Root<Y> parentRoot) {
    throw new NotImplementedException();
  }

  @Override
  public <X, Y> Join<X, Y> correlate(@Nonnull final Join<X, Y> parentJoin) {
    throw new NotImplementedException();
  }

  @Override
  public <X, Y> CollectionJoin<X, Y> correlate(@Nonnull final CollectionJoin<X, Y> parentCollection) {
    throw new NotImplementedException();
  }

  @Override
  public <X, Y> SetJoin<X, Y> correlate(@Nonnull final SetJoin<X, Y> parentSet) {
    throw new NotImplementedException();
  }

  @Override
  public <X, Y> ListJoin<X, Y> correlate(@Nonnull final ListJoin<X, Y> parentList) {
    throw new NotImplementedException();
  }

  @Override
  public <X, K, V> MapJoin<X, K, V> correlate(@Nonnull final MapJoin<X, K, V> parentMap) {
    throw new NotImplementedException();
  }

  @Override
  public AbstractQuery<?> getParent() {
    return parent;
  }

  @Override
  public CommonAbstractCriteria getContainingQuery() {
    return getParent();
  }

  /**
   * Return the selection expression.
   * @return the item to be returned in the subquery result
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Expression<T> getSelection() {
    return (Expression<T>) ((SelectionImpl) inner.getSelection()).selection;
  }

  @Override
  public Set<Join<?, ?>> getCorrelatedJoins() {
    throw new NotImplementedException();
  }

  @Override
  public <X> Root<X> from(@Nonnull final Class<X> entityClass) {
    return inner.from(entityClass);
  }

  @Override
  public <X> Root<X> from(@Nonnull final EntityType<X> entity) {
    return inner.from(entity);
  }

  @Override
  public Set<Root<?>> getRoots() {
    return inner.getRoots();
  }

  @Override
  public List<Expression<?>> getGroupList() {
    return inner.getGroupList();
  }

  @Override
  public Predicate getGroupRestriction() {
    return inner.getGroupRestriction();
  }

  @Override
  public boolean isDistinct() {
    return inner.isDistinct();
  }

  @Override
  public Class<T> getResultType() {
    return type;
  }

  @Override
  public <U> ProcessorSubquery<U> subquery(@Nonnull final Class<U> type) {
    return inner.subquery(type);
  }

  /**
   * Return the predicate that corresponds to the where clause
   * restriction(s), or null if no restrictions have been
   * specified.
   * @return where clause predicate
   */
  @Override
  public Predicate getRestriction() {
    return inner.getRestriction();
  }

  @Override
  public Predicate isNull() {
    throw new NotImplementedException();
  }

  @Override
  public Predicate isNotNull() {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(@Nonnull final Object... values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(@Nonnull final Expression<?>... values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(@Nonnull final Collection<?> values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(@Nonnull final Expression<Collection<?>> values) {
    throw new NotImplementedException();
  }

  @Override
  public <X> Expression<X> as(@Nonnull final Class<X> type) {
    throw new NotImplementedException();
  }

  @Override
  public Selection<T> alias(@Nonnull final String name) {
    throw new NotImplementedException();
  }

  /**
   * Whether the selection item is a compound selection.
   * @return boolean indicating whether the selection is a compound
   * selection
   */
  @Override
  public boolean isCompoundSelection() {
    return inner.getSelection() instanceof CompoundSelection;
  }

  /**
   * Return the selection items composing a compound selection.
   * Modifications to the list do not affect the query.
   * <p>
   * Star selections are not resolved currently!
   * @return list of selection items
   * @throws IllegalStateException if selection is not a
   * compound selection
   */
  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    if (isCompoundSelection()) {
      return new ArrayList<>(((CompoundSelection<?>) inner.getSelection()).getCompoundSelectionItems());
    } else if (inner.getSelection() != null) {
      final Selection<T> selection = inner.getSelection();
      if (selection.isCompoundSelection())
        return new ArrayList<>(selection.getCompoundSelectionItems());
      else
        return singletonList(inner.getSelection());
    } else {
      return emptyList();
    }
  }

  @Override
  public Class<? extends T> getJavaType() {
    return getResultType();
  }

  @Override
  public String getAlias() {
    throw new NotImplementedException();
  }

  @Override
  public ProcessorSubquery<T> setMaxResults(final Integer maxResult) {
    this.maxResult = Optional.ofNullable(maxResult);
    return this;
  }

  @Override
  public ProcessorSubquery<T> setFirstResult(final Integer startPosition) {
    this.firstResult = Optional.ofNullable(startPosition);
    return this;
  }

  @Override
  public StringBuilder asSQL(@Nonnull final StringBuilder statement) {
    return ((SqlConvertible) inner).asSQL(statement)
        .append(maxResult.map(i -> " LIMIT " + i).orElse(""))
        .append(firstResult.map(i -> " OFFSET " + i).orElse(""));
  }

  @Override
  public ProcessorSubquery<T> multiselect(final Selection<?>... selections) {
    inner.multiselect(selections);
    return this;
  }

  @Override
  public ProcessorSubquery<T> multiselect(final List<Selection<?>> selectionList) {
    inner.multiselect(selectionList);
    return this;
  }

  @Override
  public ProcessorSubquery<T> orderBy(final List<Order> o) {
    inner.orderBy(o);
    return this;
  }

  @Override
  public ProcessorSubquery<T> orderBy(final Order... o) {
    inner.orderBy(o);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> Root<X> from(final ProcessorSubquery<?> subquery) {
    return (Root<X>) inner.from(subquery);
  }
}
