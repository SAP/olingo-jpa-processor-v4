package com.sap.olingo.jpa.processor.cb.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.QueryTimeoutException;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.criteria.CriteriaUpdate;

class UpdateQueryImpl extends AbstractQueryImpl {

  private final CriteriaUpdateImpl<?> updateQuery;
  private final EntityManager em;

  public UpdateQueryImpl(final EntityManager em, final CriteriaUpdate<?> updateQuery) {
    super(((CriteriaUpdateImpl<?>) updateQuery).getParameterBuffer());
    this.updateQuery = (CriteriaUpdateImpl<?>) updateQuery;
    this.em = em;
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public List<?> getResultList() {
    throw new IllegalStateException();
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public Object getSingleResult() {
    throw new IllegalStateException();
  }

  /**
   * Execute an update or delete statement.
   * @return the number of entities updated or deleted
   * @throws IllegalStateException if called for a Jakarta
   * Persistence query language SELECT statement or for
   * a criteria query
   * @throws TransactionRequiredException if there is
   * no transaction or the persistence context has not
   * been joined to the transaction
   * @throws QueryTimeoutException if the statement execution
   * exceeds the query timeout value set and only
   * the statement is rolled back
   * @throws PersistenceException if the query execution exceeds
   * the query timeout value set and the transaction
   * is rolled back
   */
  @Override
  public int executeUpdate() {
    final var query = em.createNativeQuery(updateQuery.asSQL(new StringBuilder()).toString());
    copyParameter(query);
    return query.executeUpdate();
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public Query setMaxResults(final int maxResult) {
    throw new IllegalStateException();
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public int getMaxResults() {
    return 0;
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public Query setFirstResult(final int startPosition) {
    throw new IllegalStateException();
  }

  /**
   * @throws IllegalStateException
   */
  @Override
  public int getFirstResult() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(final Class<T> clazz) {
    if (clazz.isAssignableFrom(this.getClass())) {
      return (T) this;
    }
    throw new PersistenceException("Unable to unwrap " + clazz.getName());
  }

}
