package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAOperator {
  public Object get() throws ODataApplicationException;
}