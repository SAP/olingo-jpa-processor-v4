package com.sap.olingo.jpa.processor.cb.api;

import java.util.Map;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.impl.EntityManagerWrapper;

public final class EntityManagerFactoryWrapper implements EntityManagerFactory {
  private final EntityManagerFactory emf;
  private final JPAServiceDocument sd;

  public EntityManagerFactoryWrapper(final EntityManagerFactory emf, final JPAServiceDocument sd) {
    super();
    this.emf = emf;
    this.sd = sd;
  }

  @Override
  public EntityManager createEntityManager() {
    return new EntityManagerWrapper(emf.createEntityManager(), sd);
  }

  @Override
  public EntityManager createEntityManager(@SuppressWarnings("rawtypes") final Map map) {
    return new EntityManagerWrapper(emf.createEntityManager(map), sd);
  }

  @Override
  public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
    return new EntityManagerWrapper(emf.createEntityManager(synchronizationType), sd);
  }

  @Override
  public EntityManager createEntityManager(final SynchronizationType synchronizationType,
      @SuppressWarnings("rawtypes") final Map map) {
    return new EntityManagerWrapper(emf.createEntityManager(synchronizationType, map), sd);
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    try (EntityManager em = new EntityManagerWrapper(emf.createEntityManager(), sd)) {
      return em.getCriteriaBuilder();
    }
  }

  @Override
  public Metamodel getMetamodel() {
    return emf.getMetamodel();
  }

  @Override
  public boolean isOpen() {
    return emf.isOpen();
  }

  @Override
  public void close() {
    emf.close();
  }

  @Override
  public Map<String, Object> getProperties() {
    return emf.getProperties();
  }

  @Override
  public Cache getCache() {
    return emf.getCache();
  }

  @Override
  public PersistenceUnitUtil getPersistenceUnitUtil() {
    return emf.getPersistenceUnitUtil();
  }

  @Override
  public void addNamedQuery(final String name, final Query query) {
    emf.addNamedQuery(name, query);
  }

  @Override
  public <T> T unwrap(final Class<T> clazz) {
    return emf.unwrap(clazz);
  }

  @Override
  public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
    emf.addNamedEntityGraph(graphName, entityGraph);
  }

}
