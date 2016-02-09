package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;

interface JPAOperator {
  public Object get() throws ODataApplicationException;
}