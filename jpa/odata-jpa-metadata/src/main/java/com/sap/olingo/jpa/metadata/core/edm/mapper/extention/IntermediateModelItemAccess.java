package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

public interface IntermediateModelItemAccess extends JPAElement {

  boolean ignore();

  /**
   * Enables to overwrite the External, OData, name of a model item.
   * @param externalName
   */
  void setExternalName(String externalName);

  /**
   * Enables to switch if a model item shall be ignored during runtime.
   * @param ignore
   */
  void setIgnore(boolean ignore);

}