package com.sap.olingo.jpa.processor.core.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPATopLevelEntity;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;

final class JPARequestEntityImpl implements JPARequestEntity {
  private static final JPAModifyUtil utility = new JPAModifyUtil();

  private final JPAEntityType et;
  private final Map<String, Object> jpaAttributes;
  private final Map<String, Object> jpaKeys;
  private final Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities;
  private final Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks;
  private final Map<String, List<String>> odataHeaders;
  private Optional<Object> beforeImage;
  private final JPAODataRequestContextAccess requestContext;
  private final Optional<JPATopLevelEntity> topLevel;

  JPARequestEntityImpl(
      final Optional<JPATopLevelEntity> topLevel,
      final JPAEntityType et,
      final Map<String, Object> jpaAttributes,
      final Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities,
      final Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks, final Map<String, Object> keys,
      final Map<String, List<String>> headers, final JPAODataRequestContextAccess requestContext) {

    super();
    this.topLevel = topLevel;
    this.et = et;
    this.jpaAttributes = jpaAttributes;
    this.jpaDeepEntities = jpaDeepEntities;
    this.jpaLinks = jpaLinks;
    this.jpaKeys = keys;
    this.odataHeaders = headers;
    this.requestContext = requestContext;
  }

  @Override
  public Map<String, List<String>> getAllHeader() {
    return odataHeaders;
  }

  @Override
  public Optional<Object> getBeforeImage() {
    return beforeImage;
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaims() {
    return requestContext.getClaimsProvider();
  }

  @Override
  public Map<String, Object> getData() {
    return jpaAttributes;
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public Map<String, Object> getKeys() {
    return jpaKeys;
  }

  @Override
  public JPAModifyUtil getModifyUtil() {
    return utility;
  }

  @Override
  public Map<JPAAssociationPath, List<JPARequestEntity>> getRelatedEntities() {
    return jpaDeepEntities;
  }

  @Override
  public Map<JPAAssociationPath, List<JPARequestLink>> getRelationLinks() {
    return jpaLinks;
  }

  void setBeforeImage(final Optional<Object> beforeImage) {
    this.beforeImage = beforeImage;
  }

  @Override
  public List<String> getGroups() {
    final Optional<JPAODataGroupProvider> provider = requestContext.getGroupsProvider();
    return provider.isPresent() ? provider.get().getGroups() : Collections.emptyList();
  }

  @Override
  public Optional<JPATopLevelEntity> getTopLevelEntity() {
    return topLevel;
  }
}
