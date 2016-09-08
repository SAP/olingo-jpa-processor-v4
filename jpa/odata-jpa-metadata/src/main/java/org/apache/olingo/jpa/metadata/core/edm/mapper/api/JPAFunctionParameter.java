package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAFunctionParameter {

  public String getDBName();

  public Class<?> getType();

  public String getName();

  public Integer getMaxLength();

  public Integer getPrecision();

  public Integer getScale();

  public FullQualifiedName getTypeFQN() throws ODataJPAModelException;
}
