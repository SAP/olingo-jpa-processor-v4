package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public interface JPAFunction {

  public String getDBName();

  public List<JPAFunctionParameter> getParameter();

  public FullQualifiedName getReturnType();

}
