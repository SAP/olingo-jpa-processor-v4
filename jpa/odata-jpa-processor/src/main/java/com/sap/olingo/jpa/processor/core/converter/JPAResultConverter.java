package com.sap.olingo.jpa.processor.core.converter;

import java.util.Collection;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public interface JPAResultConverter {

  public Object getResult(final JPAExpandResult jpaResult, final Collection<JPAPath> reqestedSelection)
      throws ODataApplicationException;

}
