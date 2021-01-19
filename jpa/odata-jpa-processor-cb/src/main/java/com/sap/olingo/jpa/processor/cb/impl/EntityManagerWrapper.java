package com.sap.olingo.jpa.processor.cb.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

public class EntityManagerWrapper implements EntityManager { // NOSONAR
  private static final Log LOG = LogFactory.getLog(EntityManagerWrapper.class);
  private Optional<ProcessorCriteriaBuilder> cb;
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

  /**
   * Make an instance managed and persistent.
   * @param entity entity instance
   * @throws EntityExistsException if the entity already exists.
   * (If the entity already exists, the <code>EntityExistsException</code> may
   * be thrown when the persist operation is invoked, or the
   * <code>EntityExistsException</code> or another <code>PersistenceException</code> may be
   * thrown at flush or commit time.)
   * @throws IllegalArgumentException if the instance is not an
   * entity
   * @throws TransactionRequiredException if there is no transaction when
   * invoked on a container-managed entity manager of that is of type
   * <code>PersistenceContextType.TRANSACTION</code>
   */
  @Override
  public void persist(final Object entity) {
    em.persist(entity);
  }

  /**
   * Merge the state of the given entity into the
   * current persistence context.
   * @param entity entity instance
   * @return the managed instance that the state was merged to
   * @throws IllegalArgumentException if instance is not an
   * entity or is a removed entity
   * @throws TransactionRequiredException if there is no transaction when
   * invoked on a container-managed entity manager of that is of type
   * <code>PersistenceContextType.TRANSACTION</code>
   */
  @Override
  public <T> T merge(final T entity) {
    return em.merge(entity);
  }

  /**
   * Remove the entity instance.
   * @param entity entity instance
   * @throws IllegalArgumentException if the instance is not an
   * entity or is a detached entity
   * @throws TransactionRequiredException if invoked on a
   * container-managed entity manager of type
   * <code>PersistenceContextType.TRANSACTION</code> and there is
   * no transaction
   */
  @Override
  public void remove(final Object entity) {
    em.remove(entity);
  }

  /**
   * Find by primary key.
   * Search for an entity of the specified class and primary key.
   * If the entity instance is contained in the persistence context,
   * it is returned from there.
   * @param entityClass entity class
   * @param primaryKey primary key
   * @return the found entity instance or null if the entity does
   * not exist
   * @throws IllegalArgumentException if the first argument does
   * not denote an entity type or the second argument is
   * is not a valid type for that entity's primary key or
   * is null
   */
  @Override
  public <T> T find(final Class<T> entityClass, final Object primaryKey) {
    return em.find(entityClass, primaryKey);
  }

  /**
   * Find by primary key, using the specified properties.
   * Search for an entity of the specified class and primary key.
   * If the entity instance is contained in the persistence
   * context, it is returned from there.
   * If a vendor-specific property or hint is not recognized,
   * it is silently ignored.
   * @param entityClass entity class
   * @param primaryKey primary key
   * @param properties standard and vendor-specific properties
   * and hints
   * @return the found entity instance or null if the entity does
   * not exist
   * @throws IllegalArgumentException if the first argument does
   * not denote an entity type or the second argument is
   * is not a valid type for that entity's primary key or
   * is null
   * @since Java Persistence 2.0
   */
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

  /**
   * Clear the persistence context, causing all managed
   * entities to become detached. Changes made to entities that
   * have not been flushed to the database will not be
   * persisted.
   */
  @Override
  public void clear() {
    em.clear();
  }

  /**
   * Remove the given entity from the persistence context, causing
   * a managed entity to become detached. Unflushed changes made
   * to the entity if any (including removal of the entity),
   * will not be synchronized to the database. Entities which
   * previously referenced the detached entity will continue to
   * reference it.
   * @param entity entity instance
   * @throws IllegalArgumentException if the instance is not an
   * entity
   * @since Java Persistence 2.0
   */
  @Override
  public void detach(final Object entity) {
    em.detach(entity);
  }

  /**
   * Check if the instance is a managed entity instance belonging
   * to the current persistence context.
   * @param entity entity instance
   * @return boolean indicating if entity is in persistence context
   * @throws IllegalArgumentException if not an entity
   */
  @Override
  public boolean contains(final Object entity) {
    return em.contains(entity);
  }

  /**
   * Get the current lock mode for the entity instance.
   * @param entity entity instance
   * @return lock mode
   * @throws TransactionRequiredException if there is no
   * transaction or if the entity manager has not been
   * joined to the current transaction
   * @throws IllegalArgumentException if the instance is not a
   * managed entity and a transaction is active
   * @since Java Persistence 2.0
   */
  @Override
  public LockModeType getLockMode(final Object entity) {
    return em.getLockMode(entity);
  }

  /**
   * Set an entity manager property or hint.
   * If a vendor-specific property or hint is not recognized, it is
   * silently ignored.
   * @param propertyName name of property or hint
   * @param value value for property or hint
   * @throws IllegalArgumentException if the second argument is
   * not valid for the implementation
   * @since Java Persistence 2.0
   */
  @Override
  public void setProperty(final String propertyName, final Object value) {
    em.setProperty(propertyName, value);
  }

  /**
   * Get the properties and hints and associated values that are in effect
   * for the entity manager. Changing the contents of the map does
   * not change the configuration in effect.
   * @return map of properties and hints in effect for entity manager
   * @since Java Persistence 2.0
   */
  @Override
  public Map<String, Object> getProperties() {
    return em.getProperties();
  }

  /**
   * Create an instance of <code>Query</code> for executing a
   * Java Persistence query language statement.
   * @param qlString a Java Persistence query string
   * @return the new query instance
   * @throws IllegalArgumentException if the query string is
   * found to be invalid
   */
  @Override
  public Query createQuery(final String qlString) {
    return em.createQuery(qlString);
  }

  /**
   * Create an instance of <code>TypedQuery</code> for executing a
   * criteria query.
   * @param criteriaQuery a criteria query object
   * @return the new query instance
   * @throws IllegalArgumentException if the criteria query is
   * found to be invalid
   * @since Java Persistence 2.0
   */
  @Override
  public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
    return new TypedQueryImpl<>(criteriaQuery, this, parameterBuffer);
  }

  /**
   * Create an instance of <code>Query</code> for executing a criteria
   * update query.
   * @param updateQuery a criteria update query object
   * @return the new query instance
   * @throws IllegalArgumentException if the update query is
   * found to be invalid
   * @since Java Persistence 2.1
   */
  @Override
  public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaUpdate updateQuery) {
    return em.createQuery(updateQuery);
  }

  /**
   * Create an instance of <code>Query</code> for executing a criteria
   * delete query.
   * @param deleteQuery a criteria delete query object
   * @return the new query instance
   * @throws IllegalArgumentException if the delete query is
   * found to be invalid
   * @since Java Persistence 2.1
   */
  @Override
  public Query createQuery(@SuppressWarnings("rawtypes") final CriteriaDelete deleteQuery) {
    return em.createQuery(deleteQuery);
  }

  /**
   * Create an instance of <code>TypedQuery</code> for executing a
   * Java Persistence query language statement.
   * The select list of the query must contain only a single
   * item, which must be assignable to the type specified by
   * the <code>resultClass</code> argument.
   * @param qlString a Java Persistence query string
   * @param resultClass the type of the query result
   * @return the new query instance
   * @throws IllegalArgumentException if the query string is found
   * to be invalid or if the query result is found to
   * not be assignable to the specified type
   * @since Java Persistence 2.0
   */
  @Override
  public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
    throw new NotImplementedException();
  }

  /**
   * Create an instance of <code>Query</code> for executing a named query
   * (in the Java Persistence query language or in native SQL).
   * @param name the name of a query defined in metadata
   * @return the new query instance
   * @throws IllegalArgumentException if a query has not been
   * defined with the given name or if the query string is
   * found to be invalid
   */
  @Override
  public Query createNamedQuery(final String name) {
    LOG.trace("Create query: " + name);
    return em.createNamedQuery(name);
  }

  /**
   * Create an instance of <code>TypedQuery</code> for executing a
   * Java Persistence query language named query.
   * The select list of the query must contain only a single
   * item, which must be assignable to the type specified by
   * the <code>resultClass</code> argument.
   * @param name the name of a query defined in metadata
   * @param resultClass the type of the query result
   * @return the new query instance
   * @throws IllegalArgumentException if a query has not been
   * defined with the given name or if the query string is
   * found to be invalid or if the query result is found to
   * not be assignable to the specified type
   * @since Java Persistence 2.0
   */
  @Override
  public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
    LOG.trace("Create query: " + name);
    return em.createNamedQuery(name, resultClass);
  }

  /**
   * Create an instance of <code>Query</code> for executing
   * a native SQL statement, e.g., for update or delete.
   * If the query is not an update or delete query, query
   * execution will result in each row of the SQL result
   * being returned as a result of type Object[] (or a result
   * of type Object if there is only one column in the select
   * list.) Column values are returned in the order of their
   * appearance in the select list and default JDBC type
   * mappings are applied.
   * @param sqlString a native SQL query string
   * @return the new query instance
   */
  @Override
  public Query createNativeQuery(final String sqlString) {
    LOG.trace(sqlString);
    return em.createNativeQuery(sqlString);
  }

  /**
   * Create an instance of <code>Query</code> for executing
   * a native SQL query.
   * @param sqlString a native SQL query string
   * @param resultClass the class of the resulting instance(s)
   * @return the new query instance
   */
  @Override
  public Query createNativeQuery(final String sqlString, @SuppressWarnings("rawtypes") final Class resultClass) {
    LOG.trace(sqlString);
    return em.createNativeQuery(sqlString, resultClass);
  }

  /**
   * Create an instance of <code>Query</code> for executing
   * a native SQL query.
   * @param sqlString a native SQL query string
   * @param resultSetMapping the name of the result set mapping
   * @return the new query instance
   */
  @Override
  public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
    throw new NotImplementedException();
  }

  /**
   * Create an instance of <code>StoredProcedureQuery</code> for executing a
   * stored procedure in the database.
   * <p>Parameters must be registered before the stored procedure can
   * be executed.
   * <p>If the stored procedure returns one or more result sets,
   * any result set will be returned as a list of type Object[].
   * @param name name assigned to the stored procedure query
   * in metadata
   * @return the new stored procedure query instance
   * @throws IllegalArgumentException if a query has not been
   * defined with the given name
   * @since Java Persistence 2.1
   */
  @Override
  public StoredProcedureQuery createNamedStoredProcedureQuery(final String name) {
    return em.createNamedStoredProcedureQuery(name);
  }

  /**
   * Create an instance of <code>StoredProcedureQuery</code> for executing a
   * stored procedure in the database.
   * <p>Parameters must be registered before the stored procedure can
   * be executed.
   * <p>If the stored procedure returns one or more result sets,
   * any result set will be returned as a list of type Object[].
   * @param procedureName name of the stored procedure in the
   * database
   * @return the new stored procedure query instance
   * @throws IllegalArgumentException if a stored procedure of the
   * given name does not exist (or the query execution will
   * fail)
   * @since Java Persistence 2.1
   */
  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName) {
    return em.createStoredProcedureQuery(procedureName);
  }

  /**
   * Create an instance of <code>StoredProcedureQuery</code> for executing a
   * stored procedure in the database.
   * <p>Parameters must be registered before the stored procedure can
   * be executed.
   * <p>The <code>resultClass</code> arguments must be specified in the order in
   * which the result sets will be returned by the stored procedure
   * invocation.
   * @param procedureName name of the stored procedure in the
   * database
   * @param resultClasses classes to which the result sets
   * produced by the stored procedure are to
   * be mapped
   * @return the new stored procedure query instance
   * @throws IllegalArgumentException if a stored procedure of the
   * given name does not exist (or the query execution will
   * fail)
   * @since Java Persistence 2.1
   */
  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName,
      final @SuppressWarnings("rawtypes") Class... resultClasses) {
    return em.createStoredProcedureQuery(procedureName, resultClasses);
  }

  /**
   * Create an instance of <code>StoredProcedureQuery</code> for executing a
   * stored procedure in the database.
   * <p>Parameters must be registered before the stored procedure can
   * be executed.
   * <p>The <code>resultSetMapping</code> arguments must be specified in the order
   * in which the result sets will be returned by the stored
   * procedure invocation.
   * @param procedureName name of the stored procedure in the
   * database
   * @param resultSetMappings the names of the result set mappings
   * to be used in mapping result sets
   * returned by the stored procedure
   * @return the new stored procedure query instance
   * @throws IllegalArgumentException if a stored procedure or
   * result set mapping of the given name does not exist
   * (or the query execution will fail)
   */
  @Override
  public StoredProcedureQuery createStoredProcedureQuery(final String procedureName,
      final String... resultSetMappings) {
    return em.createStoredProcedureQuery(procedureName, resultSetMappings);
  }

  /**
   * Indicate to the entity manager that a JTA transaction is
   * active and join the persistence context to it.
   * <p>This method should be called on a JTA application
   * managed entity manager that was created outside the scope
   * of the active transaction or on an entity manager of type
   * <code>SynchronizationType.UNSYNCHRONIZED</code> to associate
   * it with the current JTA transaction.
   * @throws TransactionRequiredException if there is
   * no transaction
   */
  @Override
  public void joinTransaction() {
    em.joinTransaction();
  }

  /**
   * Determine whether the entity manager is joined to the
   * current transaction. Returns false if the entity manager
   * is not joined to the current transaction or if no
   * transaction is active
   * @return boolean
   * @since Java Persistence 2.1
   */
  @Override
  public boolean isJoinedToTransaction() {
    return em.isJoinedToTransaction();
  }

  /**
   * Return an object of the specified type to allow access to the
   * provider-specific API. If the provider's <code>EntityManager</code>
   * implementation does not support the specified class, the
   * <code>PersistenceException</code> is thrown.
   * @param cls the class of the object to be returned. This is
   * normally either the underlying <code>EntityManager</code> implementation
   * class or an interface that it implements.
   * @return an instance of the specified class
   * @throws PersistenceException if the provider does not
   * support the call
   * @since Java Persistence 2.0
   *
   * @Override
   * public <T> T unwrap(final Class<T> cls) {
   * return em.unwrap(cls);
   * }
   *
   * /**
   * Return the underlying provider object for the <code>EntityManager</code>,
   * if available. The result of this method is implementation
   * specific.
   * <p>The <code>unwrap</code> method is to be preferred for new applications.
   * @return underlying provider object for EntityManager
   */
  @Override
  public Object getDelegate() {
    return em.getDelegate();
  }

  /**
   * Return an object of the specified type to allow access to the
   * provider-specific API. If the provider's <code>EntityManager</code>
   * implementation does not support the specified class, the
   * <code>PersistenceException</code> is thrown.
   * @param cls the class of the object to be returned. This is
   * normally either the underlying <code>EntityManager</code> implementation
   * class or an interface that it implements.
   * @return an instance of the specified class
   * @throws PersistenceException if the provider does not
   * support the call
   * @since Java Persistence 2.0
   */
  @Override
  public <T> T unwrap(final Class<T> cls) {
    return em.unwrap(cls);
  }

  /**
   * Close an application-managed entity manager.
   * After the close method has been invoked, all methods
   * on the <code>EntityManager</code> instance and any
   * <code>Query</code>, <code>TypedQuery</code>, and
   * <code>StoredProcedureQuery</code> objects obtained from
   * it will throw the <code>IllegalStateException</code>
   * except for <code>getProperties</code>,
   * <code>getTransaction</code>, and <code>isOpen</code> (which will return false).
   * If this method is called when the entity manager is
   * joined to an active transaction, the persistence
   * context remains managed until the transaction completes.
   * @throws IllegalStateException if the entity manager
   * is container-managed
   */
  @Override
  public void close() {
    em.close();
  }

  /**
   * Determine whether the entity manager is open.
   * @return true until the entity manager has been closed
   */
  @Override
  public boolean isOpen() {
    return em.isOpen();
  }

  /**
   * Return the resource-level <code>EntityTransaction</code> object.
   * The <code>EntityTransaction</code> instance may be used serially to
   * begin and commit multiple transactions.
   * @return EntityTransaction instance
   * @throws IllegalStateException if invoked on a JTA
   * entity manager
   */
  @Override
  public EntityTransaction getTransaction() {
    return em.getTransaction();
  }

  /**
   * Return the entity manager factory for the entity manager.
   * @return EntityManagerFactory instance
   * @throws IllegalStateException if the entity manager has
   * been closed
   * @since Java Persistence 2.0
   */
  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return em.getEntityManagerFactory();
  }

  /**
   * Return an instance of <code>CriteriaBuilder</code> for the creation of
   * <code>CriteriaQuery</code> objects.
   * @return CriteriaBuilder instance
   * @throws IllegalStateException if the entity manager has
   * been closed
   * @since Java Persistence 2.0
   */
  @Override
  public ProcessorCriteriaBuilder getCriteriaBuilder() {
    if (!em.isOpen())
      throw new IllegalStateException("Entity Manager had been closed");
    return cb.orElseGet(() -> {
      cb = Optional.of(new CriteriaBuilderImpl(sd, parameterBuffer));
      return cb.get();
    });
  }

  /**
   * Return an instance of <code>Metamodel</code> interface for access to the
   * metamodel of the persistence unit.
   * @return Metamodel instance
   * @throws IllegalStateException if the entity manager has
   * been closed
   * @since Java Persistence 2.0
   */
  @Override
  public Metamodel getMetamodel() {
    return em.getMetamodel();
  }

  /**
   * Return a mutable EntityGraph that can be used to dynamically create an
   * EntityGraph.
   * @param rootType class of entity graph
   * @return entity graph
   * @since Java Persistence 2.1
   */
  @Override
  public <T> EntityGraph<T> createEntityGraph(final Class<T> rootType) {
    return em.createEntityGraph(rootType);
  }

  /**
   * Return a mutable copy of the named EntityGraph. If there
   * is no entity graph with the specified name, null is returned.
   * @param graphName name of an entity graph
   * @return entity graph
   * @since Java Persistence 2.1
   */
  @Override
  public EntityGraph<?> createEntityGraph(final String graphName) {
    return em.createEntityGraph(graphName);
  }

  /**
   * Return a named EntityGraph. The returned EntityGraph
   * should be considered immutable.
   * @param graphName name of an existing entity graph
   * @return named entity graph
   * @throws IllegalArgumentException if there is no EntityGraph of
   * the given name
   * @since Java Persistence 2.1
   */
  @Override
  public EntityGraph<?> getEntityGraph(final String graphName) {
    return em.getEntityGraph(graphName);
  }

  /**
   * Return all named EntityGraphs that have been defined for the provided
   * class type.
   * @param entityClass entity class
   * @return list of all entity graphs defined for the entity
   * @throws IllegalArgumentException if the class is not an entity
   * @since Java Persistence 2.1
   */
  @Override
  public <T> List<EntityGraph<? super T>> getEntityGraphs(final Class<T> entityClass) {
    return em.getEntityGraphs(entityClass);
  }
}
