package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

public interface JPAOperation extends JPAElement {
  /**
   *
   * @return The return or result parameter of the function
   */
  public JPAOperationResultParameter getResultParameter();

  public CsdlReturnType getReturnType();
}
