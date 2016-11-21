package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.HashMap;

public interface JPADescriptionAttribute extends JPAAttribute {

  public boolean isLocationJoin();

  public JPAAttribute getDescriptionAttribute();

  public JPAPath getLocaleFieldName();

  public HashMap<JPAPath, String> getFixedValueAssignment();

}
