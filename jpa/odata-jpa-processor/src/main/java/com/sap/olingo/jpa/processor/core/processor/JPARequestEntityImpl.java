package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;

final class JPARequestEntityImpl implements JPARequestEntity {
  private static final JPAModifyUtil util = new JPAModifyUtil();

  private final JPAEntityType et;
  private final Map<String, Object> jpaAttributes;
  private final Map<String, Object> jpaKeys;
  private final Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities;
  private final Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks;
  private final Map<String, List<String>> odataHeaders;
  private Optional<Object> beforeImage;

  JPARequestEntityImpl(JPAEntityType et, Map<String, Object> jpaAttributes,
      Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities,
      Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks, Map<String, Object> keys,
      Map<String, List<String>> headers) {
    super();
    this.et = et;
    this.jpaAttributes = jpaAttributes;
    this.jpaDeepEntities = jpaDeepEntities;
    this.jpaLinks = jpaLinks;
    this.jpaKeys = keys;
    this.odataHeaders = headers;
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
  public Optional<JPAODataClaimsProvider> getClaims() {
    return Optional.empty();
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
    return util;
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
}
