package com.sap.olingo.jpa.processor.core.processor;

import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPARequestLink {
  /**
   * Provides an instance of the target entity metadata
   * @return
   */
  public JPAEntityType getEntityType();

  /**
   * Map of related keys
   * @return
   */
  public Map<String, Object> getRelatedKeys();

  public Map<String, Object> getValues();
}
