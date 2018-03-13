package com.sap.olingo.jpa.processor.core.converter;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAResultConverter {
  public Object getResult(JPAExpandResult jpaResult) throws ODataApplicationException;

}
