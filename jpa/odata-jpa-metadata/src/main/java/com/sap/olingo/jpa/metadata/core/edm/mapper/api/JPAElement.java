package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public interface JPAElement {
  /**
   * Returns the full qualified name of an element
   * @return
   */
  public FullQualifiedName getExternalFQN();

  /**
   * Returns the element name published by the API
   * @return
   */
  public String getExternalName();

  /**
   * Returns the internally used (Java) name for an element
   * @return
   */
  public String getInternalName();
}
