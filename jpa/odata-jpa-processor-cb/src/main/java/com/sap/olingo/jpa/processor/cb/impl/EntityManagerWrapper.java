package com.sap.olingo.jpa.processor.cb.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

public class EntityManagerWrapper implements EntityManager {
  private Optional<CriteriaBuilder> cb;
  private final EntityManager em;
  private final JPAServiceDocument sd;
  private final ParameterBuffer parameterBuffer;

  public EntityManagerWrapper(final EntityManager em, final JPAServiceDocument sd) {
    super();
    this.em = em;
    this.sd = sd;
    this.cb = Optional.empty();
    this.parameterBuffer = new ParameterBuffer();
  }

  @Override
  public void persist(final Object entity) {
    em.persist(entity);
  }

  @Override
  public <T> T merge(final T entity) {
    return em.merge(entity);
  }

  @Override
  public void remove(final Object entity) {
    em.remove(entity);
  }

  @Override
  public <T> T find(final Class<T> entityClass, final Object primaryKey) {
    return em.find(entityClass, primaryKey);
  }

  @Override
  public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties) {
    return em.find(entityClass, primaryKey, properties);
  }

  @Override
  public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode) {
    return em.find(entityClass, primaryKey, lockMode);
  }

  @Override
  public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode,
      final Map<String, Object> properties) {
    return em.find(entityClass, primaryKey, lockMode, properties);
  }

  @Override
  public <T> T getReference(final Class<T> entityClass, final Object primaryKey) {
    return em.getReference(entityClass, primaryKey);
  }

  @Override
  public void flush() {
    em.flush();
  }

  @Override
  public void setFlushMode(final FlushModeType flushMode) {
    em.setFlushMode(flushMode);
  }

  @Override
  public FlushModeType getFlushMode() {
    return em.getFlushMode();
  }

  @Override
  public void lock(final Object entity, final LockModeType lockMode) {
    em.lock(entity, lockMode);
  }

  @Override
  public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
    em.lock(entity, lockMode, properties);
  }

  @Override
  public void refresh(final Object entity) {
    em.refresh(entity);
  }

  @Override
  public void refresh(final Object entity, final Map<String, Object> properties) {
    em.refresh(entity, properties);
  }

  @Override
  public void refresh(final Object entity, final LockModeType lockMode) {
    em.refresh(entity, lockMode);
  }

  @Override
  public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
    em.refresh(entity, lockMode, properties);
  }

  @Override
  public void clear() {
    em.clear();
  }

  @Override
  public void detach(final Object entity) {
    em.detach(entity);
  }

  @Override
  public boolean contains(final Object entity) {
    return em.contains(entity);
  }

  @Override
  public LockModeType getLockMode(final Object entity) {
    return em.getLockMode(entity);
  }

  @Override
  public void setProperty(final String propertyName, final Object value) {
    em.setProperty(propertyName, value);
  }

  @Override
  public Map<String, Object> getProperties() {
    return em.getProperties();
  }

  @Override
  public Query createQuery(final String qlString) {
    return em.createQuery(qlString);
  }

  @Override
  public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
    return new TypedQueryImpl<>(criteriaQuery, this, parameterBuffer);
  }

  @Override
  public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaUpdate updateQuery) {
    return em.createQuery(updateQuery);
  }

  @Override
  public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaDelete deleteQuery) {
    return em.createQuery(deleteQuery);
  }

  @Override
  public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
    throw new NotImplementedException();
  }

  @Override
  public Query createNamedQuery(final String name) {
    throw new NotImplementedException();
  }

  @Override
  public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
    throw new NotImplementedException();
  }

  @Override
  public Query createNativeQuery(final String sqlString) {
    return em.createNativeQuery(sqlString);
  }

  @Override
  public Query createNativeQuery(final String sqlString, @SuppressWarnings("rawtypes") final Class resultClass) {
    return em.createNativeQuery(sqlString, resultClass);
  }

  @Override
  public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
    throw new NotImplementedException();
  }

  @Override
  public StoredProcedureQuery createNamedStoredProcedureQuery(final String name) {
    return em.createNamedStoredProcedureQuery(name);
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName) {
    return em.createStoredProcedureQuery(procedureName);
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName,
      final @SuppressWarnings("rawtypes") Class... resultClasses) {
    return em.createStoredProcedureQuery(procedureName, resultClasses);
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName,
      final String... resultSetMappings) {
    return em.createStoredProcedureQuery(procedureName, resultSetMappings);
  }

  @Override
  public void joinTransaction() {
    em.joinTransaction();
  }

  @Override
  public boolean isJoinedToTransaction() {
    return em.isJoinedToTransaction();
  }

  @Override
  public <T> T unwrap(final Class<T> cls) {
    return em.unwrap(cls);
  }

  @Override
  public Object getDelegate() {
    return em.getDelegate();
  }

  @Override
  public void close() {
    em.close();
  }

  @Override
  public boolean isOpen() {
    return em.isOpen();
  }

  @Override
  public EntityTransaction getTransaction() {
    return em.getTransaction();
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return em.getEntityManagerFactory();
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    return cb.orElseGet(() -> {
      cb = Optional.of(new CriteriaBuilderImpl(sd, parameterBuffer));
      return cb.get();
    });
  }

  @Override
  public Metamodel getMetamodel() {
    return em.getMetamodel();
  }

  @Override
  public <T> EntityGraph<T> createEntityGraph(final Class<T> rootType) {
    return em.createEntityGraph(rootType);
  }

  @Override
  public EntityGraph<?> createEntityGraph(final String graphName) {
    return em.createEntityGraph(graphName);
  }

  @Override
  public EntityGraph<?> getEntityGraph(final String graphName) {
    return em.getEntityGraph(graphName);
  }

  @Override
  public <T> List<EntityGraph<? super T>> getEntityGraphs(final Class<T> entityClass) {
    return em.getEntityGraphs(entityClass);
  }

}
