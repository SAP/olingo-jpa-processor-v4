package com.sap.olingo.jpa.processor.core.database;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;

public interface JPAODataDatabaseTableFunction {

  /**
   *
   * @param <T>
   * @param uriResourceParts
   * @param jpaFunction
   * @param em
   * @return
   * @throws ODataApplicationException
   *
   * @deprecated implement
   * {@link #executeFunctionQuery(List, JPADataBaseFunction, EntityManager, JPAHttpHeaderMap, JPARequestParameterMap)}
   * instead
   */
  @Deprecated(since = "2.1.2", forRemoval = true)
  default <T> List<T> executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em) throws ODataApplicationException {
    return Collections.emptyList();
  }

  /**
   *
   * @param <T> As of now only {@link java.util.List} is supported
   * @param uriResourceParts
   * @param jpaFunction
   * @param em
   * @param headers
   * @param parameters
   * @return A primitive type, a complex type, an entity type or a list of one of those. According to the defined return
   * type
   * @throws ODataApplicationException
   */
  default Object executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em, final JPAHttpHeaderMap headers,
      final JPARequestParameterMap parameters) throws ODataApplicationException {
    return executeFunctionQuery(uriResourceParts, jpaFunction, em);
  }
}
