package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.Map;

public interface JPADescriptionAttribute extends JPAAttribute {

  public boolean isLocationJoin();

  /**
   * @return Property of description entity that contains the text/description
   */
  public JPAAttribute getDescriptionAttribute();

  public JPAPath getLocaleFieldName();

  public Map<JPAPath, String> getFixedValueAssignment();

  public JPAAssociationAttribute asAssociationAttribute();

}
