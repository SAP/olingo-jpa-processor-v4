package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Parameter;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.Query;
import jakarta.persistence.QueryTimeoutException;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;

class TypedQueryImpl<T> extends AbstractQueryImpl implements TypedQuery<T> {

  private final CriteriaQueryImpl<T> parent;
  private final ProcessorSelection<T> selection;
  private final EntityManager em;

  TypedQueryImpl(final CriteriaQuery<T> criteriaQuery, final EntityManager em,
      final ParameterBuffer parameterBuffer) {

    super(parameterBuffer);

    this.parent = (CriteriaQueryImpl<T>) criteriaQuery;
    this.parent.getResultType();
    this.selection = (ProcessorSelection<T>) parent.getSelection();
    this.em = em;
  }

  @Override
  public int executeUpdate() {
    return createNativeQuery().executeUpdate();
  }

  @Override
  public int getFirstResult() {
    return parent.getFirstResult();
  }

  @Override
  public int getMaxResults() {
    return parent.getMaxResults();
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
    final List<?> result = createNativeQuery().getResultList();
    if (parent.getResultType().isAssignableFrom(Tuple.class)) {
      if (result.isEmpty())
        return Collections.emptyList();
      final List<Entry<String, JPAPath>> selectionPath = buildSelection();
      final Map<String, Integer> index = buildSelectionIndex(selectionPath);
      final List<Entry<String, JPAAttribute>> selectionAttributes = toAttributeList(selectionPath);
      if (result.get(0).getClass().isArray()) {
        return (List<T>) ((List<Object[]>) result).stream()
            .map(item -> new TupleImpl(item, selectionAttributes, index))
            .collect(Collectors.toList()); // NOSONAR
      }
      return (List<T>) ((List<Object>) result).stream()
          .map(item -> new TupleImpl(item, selectionAttributes, index))
          .collect(Collectors.toList()); // NOSONAR
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
  public TypedQuery<T> setFirstResult(final int startPosition) {
    parent.setFirstResult(startPosition);
    return this;
  }

  @Override
  public TypedQuery<T> setFlushMode(final FlushModeType flushMode) {
    super.setFlushMode(flushMode);
    return this;
  }

  @Override
  public TypedQuery<T> setHint(final String hintName, final Object value) {
    super.setHint(hintName, value);
    return this;
  }

  @Override
  public TypedQuery<T> setLockMode(final LockModeType lockMode) {
    super.setLockMode(lockMode);
    return this;
  }

  @Override
  public TypedQuery<T> setMaxResults(final int maxResult) {
    this.parent.setMaxResults(maxResult);
    return this;
  }

  /**
   * Bind an instance of <code>java.util.Calendar</code> to a positional parameter.
   * @throws IllegalStateException Setting parameter is not supported
   */
  @Override
  public TypedQuery<T> setParameter(final int position, final Calendar value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final int position, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final int position, final Object value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final Parameter<Calendar> param, final Calendar value,
      final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public <X> TypedQuery<T> setParameter(final Parameter<X> param, final X value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Calendar value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public TypedQuery<T> setParameter(final String name, final Object value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X unwrap(final Class<X> clazz) {
    if (clazz.isAssignableFrom(this.getClass())) {
      return (X) this;
    }
    if (clazz.isAssignableFrom(parent.getClass())) {
      return (X) parent;
    }
    throw new PersistenceException("Unable to unwrap " + clazz.getName());
  }

  private List<Entry<String, JPAPath>> buildSelection() {
    return selection.getResolvedSelection();
  }

  private Map<String, Integer> buildSelectionIndex(final List<Entry<String, JPAPath>> selectionPath) {
    final int[] count = { 0 };
    return selectionPath.stream()
        .collect(Collectors.toMap(Entry::getKey, path -> count[0]++));
  }

  private List<Entry<String, JPAAttribute>> toAttributeList(final List<Entry<String, JPAPath>> selectionPath) {
    final List<Entry<String, JPAAttribute>> result = new ArrayList<>(selectionPath.size());
    for (final Entry<String, JPAPath> entity : selectionPath) {
      result.add(new ProcessorSelection.SelectionAttribute(entity.getKey(), entity.getValue().getLeaf()));
    }
    return result;
  }

  private Query createNativeQuery() {
    final StringBuilder sql = new StringBuilder();
    final Query query = em.createNativeQuery(parent.asSQL(sql).toString());
    query.setHint("eclipselink.cursor.scrollable", false); // https://wiki.eclipse.org/EclipseLink/Examples/JPA/Pagination#How_to_use_EclipseLink_Pagination
    copyParameter(query);
    return query;
  }
}
