package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

public interface JPAFunctionCall extends JPAOperator {

  @Override
  public Object get() throws ODataApplicationException;

  public MethodKind getFunction();

  public JPAOperator getParameter(int index);

  public int noParameters();

}