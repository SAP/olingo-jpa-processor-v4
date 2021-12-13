package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

public interface JPAMethodCall extends JPAOperator {

  public MethodKind getFunction();

  public JPAOperator getParameter(int index);

  /**
   * Number of parameter
   * @return
   */
  public int noParameters();

  /**
   * Returns extended by a prefix and a suffix<p>
   * Main use for method as parameter of of other methods. E.g.: contains(tolower('BE1'))
   * @param prefix
   * @param suffix
   * @return
   * @throws ODataApplicationException
   */
  public Object get(final String prefix, final String suffix) throws ODataApplicationException;

}