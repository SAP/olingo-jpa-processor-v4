package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

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
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;

class TypedQueryImpl<T> implements TypedQuery<T> {

  private static final String SET_A_PARAMETER_EXCEPTION =
      "Set a parameter is not supported. Parameter have to be forwarded from criteria query";
  private final CriteriaQueryImpl<T> parent;
  private final ProcessorSelection<T> selection;
  private final ParameterBuffer parameterBuffer;
  private final EntityManager em;
  private Optional<LockModeType> lockMode;
  private final Map<String, Object> hints;
  private Optional<FlushModeType> flushMode;

  TypedQueryImpl(final CriteriaQuery<T> criteriaQuery, final EntityManager em,
      final ParameterBuffer parameterBuffer) {

    this.parent = (CriteriaQueryImpl<T>) criteriaQuery;
    this.parent.getResultType();
    this.selection = (ProcessorSelection<T>) parent.getSelection();
    this.parameterBuffer = parameterBuffer;
    this.em = em;
    this.hints = new HashMap<>();
    this.lockMode = Optional.empty();
    this.flushMode = Optional.empty();
  }

  @Override
  public int executeUpdate() {
    return createNativeQuery().executeUpdate();
  }

  @Override
  public int getFirstResult() {
    return parent.getFirstResult();
  }

  @CheckForNull
  @Override
  public FlushModeType getFlushMode() {
    return flushMode.orElse(null);
  }

  @Override
  public Map<String, Object> getHints() {
    return this.hints;
  }

  @CheckForNull
  @Override
  public LockModeType getLockMode() {
    return lockMode.orElse(null);
  }

  @Override
  public int getMaxResults() {
    return parent.getMaxResults();
  }

  /**
   * Get the parameter object corresponding to the declared
   * positional parameter with the given position.
   * This method is not required to be supported for native
   * queries.
   * @param position position
   * @return parameter object
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Parameter<?> getParameter(final int position) {
    final Optional<?> parameter = parameterBuffer
        .getParameters()
        .values()
        .stream()
        .filter(p -> p.getPosition().equals(position))
        .findFirst();
    return (Parameter<?>) parameter
        .orElseThrow(() -> new IllegalArgumentException("No parameter with index " + position));
  }

  /**
   * Get the parameter object corresponding to the declared
   * positional parameter with the given position and type.
   * This method is not required to be supported by the provider.
   * @param position position
   * @param type type
   * @return parameter object
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist or is not assignable
   * to the type
   * @throws IllegalStateException if invoked on a native
   * query or Jakarta Persistence query language query when
   * the implementation does not support this use
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Parameter<X> getParameter(final int position, final Class<X> type) {
    final var parameter = getParameter(position);
    if (parameter.getParameterType() != null && type != null && !type.isAssignableFrom(parameter.getParameterType()))
      throw new IllegalArgumentException("Parameter at " + position + " has different type");
    return (Parameter<X>) parameter;
  }

  /**
   * Get the parameter object corresponding to the declared
   * parameter of the given name.
   * This method is not required to be supported for native
   * queries.
   * @param name parameter name
   * @return parameter object
   * @throws IllegalArgumentException if the parameter of the
   * specified name does not exist
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Parameter<?> getParameter(final String name) {
    final var position = Integer.valueOf(name);
    return getParameter(position);
  }

  /**
   * Get the parameter object corresponding to the declared
   * parameter of the given name and type.
   * This method is required to be supported for criteria queries
   * only.
   * @param name parameter name
   * @param type type
   * @return parameter object
   * @throws IllegalArgumentException if the parameter of the
   * specified name does not exist or is not assignable
   * to the type
   * @throws IllegalStateException if invoked on a native
   * query or Jakarta Persistence query language query when
   * the implementation does not support this use
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Parameter<X> getParameter(final String name, final Class<X> type) {
    final var parameter = getParameter(name);
    if (parameter.getParameterType() != null && type != null && !type.isAssignableFrom(parameter.getParameterType()))
      throw new IllegalArgumentException("Parameter with name " + name + " has different type");
    return (Parameter<X>) parameter;
  }

  /**
   * Get the parameter objects corresponding to the declared
   * parameters of the query.
   * Returns empty set if the query has no parameters.
   * This method is not required to be supported for native
   * queries.
   * @return set of the parameter objects
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Set<Parameter<?>> getParameters() {
    return parameterBuffer.getParameters()
        .values().stream().collect(Collectors.toSet());
  }

  /**
   * Return the input value bound to the positional parameter.
   * (Note that OUT parameters are unbound.)
   * @param position position
   * @return parameter value
   * @throws IllegalStateException if the parameter has not been
   * been bound
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist
   * @since 2.0
   */
  @Override
  public Object getParameterValue(final int position) {
    final Optional<?> parameter = parameterBuffer
        .getParameters()
        .values()
        .stream()
        .filter(p -> p.getPosition().equals(position))
        .findFirst();
    return parameter.orElseThrow(() -> new IllegalArgumentException("No parameter with index " + position));
  }

  /**
   * Return the input value bound to the parameter.
   * (Note that OUT parameters are unbound.)
   * @param param parameter object
   * @return parameter value
   * @throws IllegalArgumentException if the parameter is not
   * a parameter of the query
   * @throws IllegalStateException if the parameter has not been
   * been bound
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> X getParameterValue(final Parameter<X> param) {
    final Optional<ParameterExpression<Object, Object>> parameter = parameterBuffer.getParameters().values()
        .stream()
        .filter(p -> p.equals(param))
        .findFirst();

    return (X) parameter.map(ParameterExpression::getValue)
        .orElseThrow(() -> new IllegalArgumentException("Parameter unknown " + param));
  }

  @Override
  public Object getParameterValue(final String name) {
    final ParameterExpression<?, ?> parameter = (ParameterExpression<?, ?>) getParameter(name);
    return parameter.getValue();
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
  public boolean isBound(final Parameter<?> param) {
    return parameterBuffer.getParameters().containsValue(param);
  }

  @Override
  public TypedQuery<T> setFirstResult(final int startPosition) {
    parent.setFirstResult(startPosition);
    return this;
  }

  @Override
  public TypedQuery<T> setFlushMode(final FlushModeType flushMode) {
    this.flushMode = Optional.ofNullable(flushMode);
    return this;
  }

  @Override
  public TypedQuery<T> setHint(final String hintName, final Object value) {
    this.hints.put(hintName, value);
    return this;
  }

  @Override
  public TypedQuery<T> setLockMode(final LockModeType lockMode) {
    this.lockMode = Optional.ofNullable(lockMode);
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

  private void copyParameter(final Query query, final Map<Integer, ParameterExpression<Object, Object>> map) {
    map.entrySet().stream().forEach(entry -> query.setParameter(entry.getValue().getPosition(),
        entry.getValue().getValue()));
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
    copyParameter(query, parameterBuffer.getParameters());
    return query;
  }
}
