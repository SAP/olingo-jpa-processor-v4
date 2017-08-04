package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPAODataDatabaseTableFunction {
  List<?> executeFunctionQuery(UriResourceFunction uriResourceFunction, JPADataBaseFunction jpaFunction,
      JPAEntityType returnType, EntityManager em) throws ODataApplicationException;
}
