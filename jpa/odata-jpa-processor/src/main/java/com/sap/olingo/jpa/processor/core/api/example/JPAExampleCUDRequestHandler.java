package com.sap.olingo.jpa.processor.core.api.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.processor.JPAModifyUtil;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;
import com.sap.olingo.jpa.processor.core.processor.JPARequestLink;

/**
 * Example implementation at a CUD handler. The main purpose is rapid prototyping.
 * Getter /Setter required
 * @author Oliver Grande
 *
 */
public class JPAExampleCUDRequestHandler extends JPAAbstractCUDRequestHandler {
  private final Map<Object, JPARequestEntity> entityBuffer;

  public JPAExampleCUDRequestHandler() {
    entityBuffer = new HashMap<>();
  }

  @Override
  public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException {

    Object instance = null;

    if (requestEntity.getKeys().isEmpty()) {
      // POST an Entity
      instance = createOneEntity(requestEntity, em, null);
    } else {
      // POST on Link only
      // https://issues.oasis-open.org/browse/ODATA-1294
      instance = findEntity(requestEntity, em);
    }
    processRelatedEntities(requestEntity.getRelatedEntities(), instance, requestEntity.getModifyUtil(), em);
    return instance;
  }

  @Override
  public void deleteEntity(JPARequestEntity requestEntity, EntityManager em) throws ODataJPAProcessException {

    final Object instance = em.find(requestEntity.getEntityType().getTypeClass(),
        requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity.getKeys(),
            requestEntity.getEntityType()));
    if (instance != null)
      em.remove(instance);
  }

  @Override
  public JPAUpdateResult updateEntity(final JPARequestEntity requestEntity, final EntityManager em,
      final HttpMethod method) throws ODataJPAProcessException {

    if (method == HttpMethod.PATCH || method == HttpMethod.DELETE) {
      final Object instance = em.find(requestEntity.getEntityType().getTypeClass(), requestEntity.getModifyUtil()
          .createPrimaryKey(requestEntity.getEntityType(), requestEntity.getKeys(), requestEntity.getEntityType()));
      requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());

      updateLinks(requestEntity, em, instance);
      return new JPAUpdateResult(false, instance);
    }
    return super.updateEntity(requestEntity, em, method);
  }

  @Override
  public void validateChanges(EntityManager em) throws ODataJPAProcessException {
    for (Entry<Object, JPARequestEntity> entity : entityBuffer.entrySet()) {
      processBindingLinks(entity.getValue().getRelationLinks(), entity.getKey(), entity.getValue().getModifyUtil(), em);
    }
  }

  private Object createInstance(final Constructor<?> cons, final Object parent) throws ODataJPAProcessorException {

    try {
      if (cons.getParameterCount() == 1)
        return cons.newInstance(parent);
      return cons.newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Object createOneEntity(final JPARequestEntity requestEntity, final EntityManager em,
      final Object parent) throws ODataJPAProcessException {

    final Object instance = createInstance(getConstructor(requestEntity.getEntityType(), parent), parent);
    requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());
    em.persist(instance);
    entityBuffer.put(instance, requestEntity);
    return instance;
  }

  private Object findEntity(final JPARequestEntity requestEntity, EntityManager em) throws ODataJPAProcessorException,
      ODataJPAInvocationTargetException {

    final Object key = requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity
        .getKeys(), requestEntity.getEntityType());
    return em.getReference(requestEntity.getEntityType().getTypeClass(), key);
  }

  private Constructor<?> getConstructor(final JPAStructuredType st, final Object parentInstance)
      throws ODataJPAProcessorException {
    // If a parent exists, try to use a constructor that accepts the parent
    if (parentInstance != null) {
      try {
        return st.getTypeClass().getConstructor(parentInstance.getClass());
      } catch (NoSuchMethodException | SecurityException e) {} // NOSONAR
    }
    try {
      return st.getTypeClass().getConstructor();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private void processBindingLinks(final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks,
      final Object instance, final JPAModifyUtil util, final EntityManager em) throws ODataJPAProcessException {

    for (final Entry<JPAAssociationPath, List<JPARequestLink>> entity : relationLinks.entrySet()) {
      final JPAAssociationPath pathInfo = entity.getKey();
      for (final JPARequestLink requestLink : entity.getValue()) {
        final Object targetKey = util.createPrimaryKey((JPAEntityType) pathInfo.getTargetType(), requestLink
            .getRelatedKeys(), (JPAEntityType) pathInfo.getSourceType());
        final Object target = em.find(pathInfo.getTargetType().getTypeClass(), targetKey);
        util.linkEntities(instance, target, pathInfo);
      }
    }
  }

  private void processRelatedEntities(final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities,
      final Object parentInstance, final JPAModifyUtil util, final EntityManager em)
      throws ODataJPAProcessException {

    for (final Map.Entry<JPAAssociationPath, List<JPARequestEntity>> entity : relatedEntities.entrySet()) {
      final JPAAssociationPath pathInfo = entity.getKey();
      for (final JPARequestEntity requestEntity : entity.getValue()) {

        final Object newInstance = createOneEntity(requestEntity, em, parentInstance);
        util.linkEntities(parentInstance, newInstance, pathInfo);
        if (pathInfo.getPartner() != null) {
          try {
            util.linkEntities(newInstance, parentInstance, pathInfo.getPartner().getPath());
          } catch (ODataJPAModelException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
          }
        }
        processRelatedEntities(requestEntity.getRelatedEntities(), newInstance, util, em);
      }
    }
  }

  private void updateLinks(final JPARequestEntity requestEntity, final EntityManager em, final Object instance)
      throws ODataJPAProcessorException, ODataJPAInvocationTargetException {
    if (requestEntity.getRelationLinks() != null) {
      for (Entry<JPAAssociationPath, List<JPARequestLink>> links : requestEntity.getRelationLinks().entrySet()) {
        for (JPARequestLink link : links.getValue()) {
          final Object related = em.find(link.getEntityType().getTypeClass(), requestEntity.getModifyUtil()
              .createPrimaryKey(link.getEntityType(), link.getRelatedKeys(), link.getEntityType()));
          requestEntity.getModifyUtil().linkEntities(instance, related, links.getKey());
        }
      }
    }
  }
}
