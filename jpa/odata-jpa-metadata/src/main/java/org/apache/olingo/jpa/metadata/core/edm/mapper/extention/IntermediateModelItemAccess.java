package org.apache.olingo.jpa.metadata.core.edm.mapper.extention;

public interface IntermediateModelItemAccess {

  String getExternalName();

  String getInternalName();

  boolean ignore();

  void setExternalName(String externalName);

  void setIgnore(boolean ignore);

}