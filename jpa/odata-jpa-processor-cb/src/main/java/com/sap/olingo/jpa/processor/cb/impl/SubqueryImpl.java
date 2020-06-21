package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.processor.cb.api.ProcessorSubquery;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;

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
  private final CriteriaQuery<T> inner;

  SubqueryImpl(@Nonnull final Class<T> type, @Nonnull final CriteriaQuery<?> parent, final AliasBuilder ab,
      final CriteriaBuilder cb) {
    super();
    this.type = Objects.requireNonNull(type);
    this.parent = Objects.requireNonNull(parent);
    this.inner = new CriteriaQueryImpl<>(type, ((CriteriaQueryImpl<?>) parent).getServiceDocument(), ab, cb);
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X, Y> Join<X, Y> correlate(@Nonnull final Join<X, Y> parentJoin) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X, Y> CollectionJoin<X, Y> correlate(@Nonnull final CollectionJoin<X, Y> parentCollection) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X, Y> SetJoin<X, Y> correlate(@Nonnull final SetJoin<X, Y> parentSet) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X, Y> ListJoin<X, Y> correlate(@Nonnull final ListJoin<X, Y> parentList) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X, K, V> MapJoin<X, K, V> correlate(@Nonnull final MapJoin<X, K, V> parentMap) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AbstractQuery<?> getParent() {
    return parent;
  }

  @Override
  public CommonAbstractCriteria getContainingQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Expression<T> getSelection() {
    return (Expression<T>) inner.getSelection();
  }

  @Override
  public Set<Join<?, ?>> getCorrelatedJoins() {
    // TODO Auto-generated method stub
    return null;
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
  public <U> Subquery<U> subquery(@Nonnull final Class<U> type) {
    return inner.subquery(type);
  }

  @Override
  public Predicate getRestriction() {
    return inner.getGroupRestriction();
  }

  @Override
  public Predicate isNull() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate isNotNull() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(@Nonnull final Object... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(@Nonnull final Expression<?>... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(@Nonnull final Collection<?> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(@Nonnull final Expression<Collection<?>> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X> Expression<X> as(@Nonnull final Class<X> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Selection<T> alias(@Nonnull final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isCompoundSelection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<? extends T> getJavaType() {
    return type;
  }

  @Override
  public String getAlias() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessorSubquery<T> setMaxResults(final int maxResult) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public ProcessorSubquery<T> setFirstResult(final int startPosition) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public StringBuilder asSQL(@Nonnull final StringBuilder statment) {
    return ((SqlConvertible) inner).asSQL(statment);
  }

}
