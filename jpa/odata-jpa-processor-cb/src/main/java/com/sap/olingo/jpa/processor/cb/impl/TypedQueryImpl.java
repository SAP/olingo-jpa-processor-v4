package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TemporalType;
import javax.persistence.TransactionRequiredException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;

class TypedQueryImpl<T> implements TypedQuery<T> {

  private final CriteriaQueryImpl<T> parent;
  private final Query q;
  private final ProcessorSelection<T> selection;

  TypedQueryImpl(final CriteriaQuery<T> criteriaQuery, final EntityManager em,
      final ParameterBuffer parameterBuffer) {
    final StringBuilder sql = new StringBuilder();
    this.parent = (CriteriaQueryImpl<T>) criteriaQuery;
    this.parent.getResultType();
    this.selection = (ProcessorSelection<T>) parent.getSelection();
    this.q = em.createNativeQuery(parent.asSQL(sql).toString());
    copyParameter(parameterBuffer.getParameter());
  }

  @Override
  public int executeUpdate() {
    return q.executeUpdate();
  }

  @Override
  public int getFirstResult() {
    return q.getFirstResult();
  }

  @Override
  public FlushModeType getFlushMode() {
    return q.getFlushMode();
  }

  @Override
  public Map<String, Object> getHints() {
    return q.getHints();
  }

  @Override
  public LockModeType getLockMode() {
    return q.getLockMode();
  }

  @Override
  public int getMaxResults() {
    return q.getMaxResults();
  }

  @Override
  public Parameter<?> getParameter(final int position) {
    return q.getParameter(position);
  }

  @Override
  public <X> Parameter<X> getParameter(final int position, final Class<X> type) {
    return q.getParameter(position, type);
  }

  @Override
  public Parameter<?> getParameter(final String name) {
    return q.getParameter(name);
  }

  @Override
  public <X> Parameter<X> getParameter(final String name, final Class<X> type) {
    return q.getParameter(name, type);
  }

  @Override
  public Set<Parameter<?>> getParameters() {
    return q.getParameters();
  }

  @Override
  public Object getParameterValue(final int position) {
    return q.getParameterValue(position);
  }

  @Override
  public <X> X getParameterValue(final Parameter<X> param) {
    return q.getParameterValue(param);
  }

  @Override
  public Object getParameterValue(final String name) {
    return q.getParameterValue(name);
  }

  /**
   * Execute a SELECT query and return the query results as a typed List.
   * @return a list of the results
   * @throws IllegalStateException if called for a Java
   * Persistence query language UPDATE or DELETE statement
   * @throws QueryTimeoutException if the query execution exceeds
   * the query timeout value set and only the statement is
   * rolled back
   * @throws TransactionRequiredException if a lock mode other than
   * <code>NONE</code> has been set and there is no transaction
   * or the persistence context has not been joined to the
   * transaction
   * @throws PessimisticLockException if pessimistic locking
   * fails and the transaction is rolled back
   * @throws LockTimeoutException if pessimistic locking
   * fails and only the statement is rolled back
   * @throws PersistenceException if the query execution exceeds
   * the query timeout value set and the transaction
   * is rolled back
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<T> getResultList() {

    final List<?> result = q.getResultList();
    if (parent.getResultType().isAssignableFrom(Tuple.class)) {
      if (result.isEmpty())
        return Collections.emptyList();
      final List<Entry<String, JPAPath>> selPath = buildSelection();
      final Map<String, Integer> index = buildSelectionIndex(selPath);
      final List<Entry<String, JPAAttribute>> selAttributes = toAttributeList(selPath);
      if (result.get(0).getClass().isArray()) {
        return (List<T>) ((List<Object[]>) result).stream()
            .map(r -> new TupleImpl(r, selAttributes, index))
            .collect(Collectors.toList());
      }
      return (List<T>) ((List<Object>) result).stream()
          .map(r -> new TupleImpl(r, selAttributes, index))
          .collect(Collectors.toList());
    }
    return (List<T>) result;
  }

  /**
   * Execute a SELECT query that returns a single untyped result.
   * @return the result
   * @throws NoResultException if there is no result
   * @throws NonUniqueResultException if more than one result
   * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
   * @throws QueryTimeoutException if the query execution exceeds
   * the query timeout value set and only the statement is rolled back
   * @throws TransactionRequiredException if a lock mode other than
   * <code>NONE</code> has been set and there is no transaction
   * or the persistence context has not been joined to the transaction
   * @throws PessimisticLockException if pessimistic locking
   * fails and the transaction is rolled back
   * @throws LockTimeoutException if pessimistic locking
   * fails and only the statement is rolled back
   * @throws PersistenceException if the query execution exceeds
   * the query timeout value set and the transaction
   * is rolled back
   */
  @Override
  public T getSingleResult() {
    final List<T> results = getResultList();
    if (results.isEmpty())
      throw new NoResultException();
    if (results.size() > 1)
      throw new NonUniqueResultException();
    return results.get(0);
  }

  @Override
  public boolean isBound(final Parameter<?> param) {
    return q.isBound(param);
  }

  @Override
  public TypedQuery<T> setFirstResult(final int startPosition) {

    q.setFirstResult(startPosition);
    return this;
  }

  @Override
  public TypedQuery<T> setFlushMode(final FlushModeType flushMode) {
    q.setFlushMode(flushMode);
    return this;
  }

  @Override
  public TypedQuery<T> setHint(final String hintName, final Object value) {
    q.setHint(hintName, value);
    return this;
  }

  @Override
  public TypedQuery<T> setLockMode(final LockModeType lockMode) {
    q.setLockMode(lockMode);
    return this;
  }

  @Override
  public TypedQuery<T> setMaxResults(final int maxResult) {
    q.setMaxResults(maxResult);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final int position, final Calendar value, final TemporalType temporalType) {
    q.setParameter(position, value, temporalType);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final int position, final Date value, final TemporalType temporalType) {
    q.setParameter(position, value, temporalType);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final int position, final Object value) {
    q.setParameter(position, value);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final Parameter<Calendar> param, final Calendar value,
      final TemporalType temporalType) {
    q.setParameter(param, value, temporalType);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
    q.setParameter(param, value, temporalType);
    return this;
  }

  @Override
  public <X> TypedQuery<T> setParameter(final Parameter<X> param, final X value) {
    q.setParameter(param, value);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Calendar value, final TemporalType temporalType) {
    q.setParameter(name, value, temporalType);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Date value, final TemporalType temporalType) {
    q.setParameter(name, value, temporalType);
    return this;
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Object value) {
    q.setParameter(name, value);
    return this;
  }

  @Override
  public <X> X unwrap(final Class<X> cls) {
    return q.unwrap(cls);
  }

  private List<Entry<String, JPAPath>> buildSelection() {
    return selection.getResolvedSelection();
  }

  private Map<String, Integer> buildSelectionIndex(final List<Entry<String, JPAPath>> selPath) {

    final int[] count = { 0 };
    return selPath.stream()
        .collect(Collectors.toMap(Entry::getKey, p -> count[0]++));
  }

  private void copyParameter(final Map<Integer, ParameterExpression<?, ?>> map) {
    map.entrySet().stream().forEach(e -> this.q.setParameter(e.getValue().getPosition(), e.getValue().getValue()));
  }

  private List<Entry<String, JPAAttribute>> toAttributeList(final List<Entry<String, JPAPath>> selPath) {
    final List<Entry<String, JPAAttribute>> result = new ArrayList<>(selPath.size());
    for (final Entry<String, JPAPath> entity : selPath) {
      result.add(new ProcessorSelection.SelectionAttribute(entity.getKey(), entity.getValue().getLeaf()));
    }
    return result;
  }

}
