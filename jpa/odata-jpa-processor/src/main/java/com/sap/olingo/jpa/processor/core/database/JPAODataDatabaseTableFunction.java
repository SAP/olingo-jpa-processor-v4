package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;

public interface JPAODataDatabaseTableFunction {
  List<?> executeFunctionQuery(UriResourceFunction uriResourceFunction, JPAFunction jpaFunction,
      JPAEntityType returnType, EntityManager em) throws ODataApplicationException;
}
