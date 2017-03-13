package com.sap.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class JPARequestEntityImpl implements JPARequestEntity {
  private static final JPAModifyUtil util = new JPAModifyUtil();

  private final JPAEntityType et;
  private final Map<String, Object> jpaAttributes;
  private final Map<String, Object> jpaKeys;
  private final Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities;
  private final Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks;

  JPARequestEntityImpl(JPAEntityType et, Map<String, Object> jpaAttributes,
      Map<JPAAssociationPath, List<JPARequestEntity>> jpaDeepEntities,
      Map<JPAAssociationPath, List<JPARequestLink>> jpaLinks) {
    super();
    this.et = et;
    this.jpaAttributes = jpaAttributes;
    this.jpaDeepEntities = jpaDeepEntities;
    this.jpaLinks = jpaLinks;
    this.jpaKeys = new HashMap<String, Object>(0);
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
}
