package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;

public interface JPAODataDatabaseTableFunction {

  <T> List<T> executeFunctionQuery(final List<UriResource> uriResourceParts, final JPADataBaseFunction jpaFunction,
      final EntityManager em) throws ODataApplicationException;
}
