package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;

public abstract class JPAAbstractExpandQuery extends JPAAbstractJoinQuery {

  public JPAAbstractExpandQuery(final OData odata, final JPAODataSessionContextAccess sessionContext,
      final JPAEntityType jpaEntityType, final JPAODataRequestContextAccess requestContext,
      final List<JPANavigationPropertyInfo> navigationInfo) throws ODataException {

    super(odata, sessionContext, jpaEntityType, requestContext, navigationInfo);
  }

  public JPAAbstractExpandQuery(final OData odata, final JPAODataSessionContextAccess sessionContext,
      final JPAEntityType entityType, final UriInfoResource uriInfo, final JPAODataRequestContextAccess requestContext,
      final List<JPANavigationPropertyInfo> hops) throws ODataException {

    super(odata, sessionContext, entityType, uriInfo, requestContext, hops);
  }

  @Override
  public abstract JPAExpandQueryResult execute() throws ODataApplicationException;

}
