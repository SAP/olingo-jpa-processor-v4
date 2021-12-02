package com.sap.olingo.jpa.processor.core.api.example;

import static com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException.MessageKeys.ENTITY_ALREADY_EXISTS;
import static com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException.MessageKeys.ENTITY_NOT_FOUND;
import static com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException.MessageKeys.MODIFY_NOT_ALLOWED;
import static com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException.MessageKeys.WILDCARD_RANGE_NOT_SUPPORTED;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.processor.JPAModifyUtil;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;
import com.sap.olingo.jpa.processor.core.processor.JPARequestLink;

/**
 * Example implementation at a CUD handler. The main purpose is rapid prototyping.<p/>
 * The implementation requires Getter and Setter. This includes getter for collection properties and collection
 * navigation properties that return at least empty collections.<br/>
 * To link entities constructor injection is used. So each dependent entity needs a constructor that takes a entity type
 * it depends on as parameter.
 * @author Oliver Grande
 *
 */
public class JPAExampleCUDRequestHandler extends JPAAbstractCUDRequestHandler {
  private final Map<Object, JPARequestEntity> entityBuffer;
  private final LocalDateTime now;

  public JPAExampleCUDRequestHandler() {
    entityBuffer = new HashMap<>();
    // Doing so all the changes of one request get the same updatedAt
    now = LocalDateTime.now(ZoneId.of("UTC"));
    new Date();
  }

  @Override
  public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException {

    // POST an Entity
    Object instance = createOneEntity(requestEntity, null);
    if (requestEntity.getKeys().isEmpty()) {
      if (!hasGeneratedKey(requestEntity, em)) {
        final Object old = em.find(requestEntity.getEntityType().getTypeClass(),
            requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), instance));
        if (old != null)
          throw new JPAExampleModifyException(ENTITY_ALREADY_EXISTS, HttpStatusCode.BAD_REQUEST);
      } else {
        // Pre-fill granted ID, so it can be used for deep inserts
        em.persist(instance);
      }
    } else {
      // POST on Link only // https://issues.oasis-open.org/browse/ODATA-1294
      instance = findEntity(requestEntity, em);
    }
    processRelatedEntities(requestEntity.getRelatedEntities(), instance, requestEntity.getModifyUtil(), em);
    setAuditInformation(instance, requestEntity.getClaims(), true);
    em.persist(instance);
    return instance;
  }

  @Override
  public void deleteEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException {

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
      if (instance == null) throw new JPAExampleModifyException(ENTITY_NOT_FOUND, HttpStatusCode.NOT_FOUND);
      requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());

      updateLinks(requestEntity, em, instance);
      setAuditInformation(instance, requestEntity.getClaims(), false);
      return new JPAUpdateResult(false, instance);
    }
    return super.updateEntity(requestEntity, em, method);
  }

  @Override
  public void validateChanges(final EntityManager em) throws ODataJPAProcessException {

    for (final Entry<Object, JPARequestEntity> entity : entityBuffer.entrySet())
      processBindingLinks(entity.getValue().getRelationLinks(), entity.getKey(), entity.getValue().getModifyUtil(), em);
  }

  private void checkAuthorizationsOneClaim(final JPAProtectionInfo protectionInfo, final Object value,
      final List<JPAClaimsPair<?>> pairs) throws JPAExampleModifyException {
    boolean match = false;
    for (final JPAClaimsPair<?> pair : pairs)
      if (protectionInfo.supportsWildcards())
        match = checkAuthorizationsOnePairWithWildcard(value, match, pair);
      else
        match = checkAuthorizationsOnePair(value, match, pair);
    if (!match)
      throw new JPAExampleModifyException(MODIFY_NOT_ALLOWED, HttpStatusCode.FORBIDDEN);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean checkAuthorizationsOnePair(final Object value, boolean match, final JPAClaimsPair<?> pair)
      throws JPAExampleModifyException {
    if (!pair.hasUpperBoundary &&
        ("*".equals(pair.min) || value.equals(pair.min))) {
      match = true;
    } else if (pair.hasUpperBoundary) {
      if (!(value instanceof Comparable<?>))
        throw new JPAExampleModifyException(MODIFY_NOT_ALLOWED, HttpStatusCode.FORBIDDEN);
      if (((Comparable) value).compareTo(pair.min) >= 0
          && ((Comparable) value).compareTo(pair.max) <= 0)
        match = true;
    }
    return match;
  }

  private boolean checkAuthorizationsOnePairWithWildcard(final Object value, boolean match, final JPAClaimsPair<?> pair)
      throws JPAExampleModifyException {
    if (pair.hasUpperBoundary) {
      final String minPrefix = determineAuthorizationPrefix(pair.min);
      final String maxPrefix = determineAuthorizationPrefix(pair.max);
      final String minComparator = ((String) value).substring(0, minPrefix.length());
      final String maxComparator = ((String) value).substring(0, maxPrefix.length());
      if (minComparator.compareTo(minPrefix) >= 0
          && maxComparator.compareTo(maxPrefix) <= 0)
        match = true;
    } else {
      // '+' and '_' --> .
      // '*' and '%' --> .+
      final String minPattern = ((String) pair.minAs()).replace("\\.", "\\#").replaceAll("[+_]", ".")
          .replaceAll("[*%]", ".+");
      if (Pattern.matches(minPattern, (String) value))
        match = true;
    }
    return match;
  }

  private void checkAuthorities(final Object instance, final JPAStructuredType entityType,
      final Optional<JPAODataClaimProvider> claims, final JPAModifyUtil modifyUtil) throws JPAExampleModifyException {
    try {
      final List<JPAProtectionInfo> protections = entityType.getProtections();
      if (!protections.isEmpty()) {
        final JPAODataClaimProvider claimsProvider = claims.orElseThrow(
            () -> new JPAExampleModifyException(MODIFY_NOT_ALLOWED, HttpStatusCode.FORBIDDEN));
        for (final JPAProtectionInfo protectionInfo : protections) {
          final Object value = determineValue(instance, modifyUtil, protectionInfo);
          final List<JPAClaimsPair<?>> pairs = claimsProvider.get(protectionInfo.getClaimName());
          if (pairs.isEmpty())
            throw new JPAExampleModifyException(MODIFY_NOT_ALLOWED, HttpStatusCode.FORBIDDEN);
          checkAuthorizationsOneClaim(protectionInfo, value, pairs);
        }
      }
    } catch (final ODataJPAModelException | NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new JPAExampleModifyException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
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

  private Object createOneEntity(final JPARequestEntity requestEntity,
      final Object parent) throws ODataJPAProcessException {

    final Object instance = createInstance(getConstructor(requestEntity.getEntityType(), parent), parent);
    requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());
    checkAuthorities(instance, requestEntity.getEntityType(), requestEntity.getClaims(), requestEntity.getModifyUtil());
    entityBuffer.put(instance, requestEntity);
    return instance;
  }

  private String determineAuthorizationPrefix(final Object restriction) throws JPAExampleModifyException {
    final String[] minPrefix = ((String) restriction).split("[*%+_]");
    if (minPrefix.length > 1)
      throw new JPAExampleModifyException(WILDCARD_RANGE_NOT_SUPPORTED, HttpStatusCode.NOT_IMPLEMENTED);
    return minPrefix[0];
  }

  private Object determineValue(final Object instance, final JPAModifyUtil modifyUtil,
      final JPAProtectionInfo protectionInfo) throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    Object value = instance;
    for (final JPAElement element : protectionInfo.getPath().getPath()) {
      final JPAAttribute attribute = (JPAAttribute) element;
      final String getterName = "get" + modifyUtil.buildMethodNameSuffix(attribute);
      final Method getter = value.getClass().getMethod(getterName);
      value = getter.invoke(value);
    }
    return value;
  }

  private Object findEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessorException,
      ODataJPAInvocationTargetException {

    final Object key = requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity
        .getKeys(), requestEntity.getEntityType());
    return em.getReference(requestEntity.getEntityType().getTypeClass(), key);
  }

  private Constructor<?> getConstructor(final JPAStructuredType st, final Object parentInstance)
      throws ODataJPAProcessorException {
    // If a parent exists, try to use a constructor that accepts the parent
    if (parentInstance != null) try {
      return st.getTypeClass().getConstructor(parentInstance.getClass());
    } catch (NoSuchMethodException | SecurityException e) {} // NOSONAR
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
            .getRelatedKeys(), pathInfo.getSourceType());
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

        final Object newInstance = createOneEntity(requestEntity, parentInstance);
        util.linkEntities(parentInstance, newInstance, pathInfo);
        try {
          final JPAAssociationAttribute attribute = pathInfo.getPartner();
          if (attribute != null) {
            util.linkEntities(newInstance, parentInstance, attribute.getPath());
          }
        } catch (final ODataJPAModelException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        processRelatedEntities(requestEntity.getRelatedEntities(), newInstance, util, em);
      }
    }
  }

  private void setAuditInformation(final Object instance, final Optional<JPAODataClaimProvider> claims,
      final boolean created) {

    if (instance instanceof JPAExampleAuditable) {
      final JPAExampleAuditable auditable = (JPAExampleAuditable) instance;
      if (created) {
        auditable.setCreatedAt(now);
        claims.ifPresent(c -> auditable.setCreatedBy(c.user().orElse("")));
      }
      auditable.setUpdatedAt(now);
      claims.ifPresent(c -> auditable.setUpdatedBy(c.user().orElse("")));
    }
  }

  private void updateLinks(final JPARequestEntity requestEntity, final EntityManager em, final Object instance)
      throws ODataJPAProcessorException, ODataJPAInvocationTargetException {
    if (requestEntity.getRelationLinks() != null)
      for (final Entry<JPAAssociationPath, List<JPARequestLink>> links : requestEntity.getRelationLinks().entrySet()) {
        for (final JPARequestLink link : links.getValue()) {
          final Object related = em.find(link.getEntityType().getTypeClass(), requestEntity.getModifyUtil()
              .createPrimaryKey(link.getEntityType(), link.getRelatedKeys(), link.getEntityType()));
          requestEntity.getModifyUtil().linkEntities(instance, related, links.getKey());
        }
      }
  }

  private boolean hasGeneratedKey(final JPARequestEntity requestEntity, final EntityManager em) {

    final JPAEntityType et = requestEntity.getEntityType();
    return em.getMetamodel()
        .getEntities()
        .stream()
        .filter(e -> e.getName().equals(et.getExternalName()))
        .findFirst()
        .map(jpaEt -> hasGeneratedKeyInt(et, jpaEt))
        .orElse(false);
  }

  private boolean hasGeneratedKeyInt(final JPAEntityType et, final EntityType<?> jpaEt) {
    try {
      if (jpaEt.hasSingleIdAttribute()) {
        final JPAAttribute key = et.getKey().get(0);
        final SingularAttribute<?, ?> at = jpaEt.getId(key.getType());
        if (at != null &&
            ((AnnotatedElement) at.getJavaMember()).getAnnotation(GeneratedValue.class) != null)
          return true;
      }
      return false;
    } catch (final ODataJPAModelException e) {
      return false;
    }
  }
}