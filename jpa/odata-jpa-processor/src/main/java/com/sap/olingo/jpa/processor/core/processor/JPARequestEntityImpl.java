package com.sap.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class JPARequestEntityImpl implements JPARequestEntity {
  private static final JPAModifyUtil util = new JPAModifyUtil();

  private final JPAEntityType et;
  private final Map<String, Object> jpaAttributes;
  private final Map<String, Object> jpaKeys;

  JPARequestEntityImpl(JPAEntityType et, Map<String, Object> jpaAttributes) {
    super();
    this.et = et;
    this.jpaAttributes = jpaAttributes;
    this.jpaKeys = new HashMap<String, Object>(0);
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public Map<String, Object> getData() {
    return jpaAttributes;
  }

  @Override
  public Map<String, Object> getKeys() {
    return jpaKeys;
  }

  @Override
  public JPAModifyUtil getModifyUtil() {
    return util;
  }
}
