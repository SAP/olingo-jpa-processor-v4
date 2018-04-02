package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.Map;

public interface JPADescriptionAttribute extends JPAAttribute {

  public boolean isLocationJoin();

  public JPAAttribute getDescriptionAttribute();

  public JPAPath getLocaleFieldName();

  public Map<JPAPath, String> getFixedValueAssignment();

}
