package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPADescriptionAttribute extends JPAAttribute {

  public boolean isLocationJoin();

  public JPAAttribute getDescriptionAttribute();

  public JPAPath getLocaleFieldName() throws ODataJPAModelException;

}
