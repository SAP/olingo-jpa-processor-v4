package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

public interface IntermediateModelItemAccess extends JPAElement {

  boolean ignore();

  void setExternalName(String externalName);

  void setIgnore(boolean ignore);

}