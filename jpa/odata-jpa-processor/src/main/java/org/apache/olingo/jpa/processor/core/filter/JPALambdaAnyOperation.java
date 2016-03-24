package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;

class JPALambdaAnyOperation extends JPALambdaOperation implements JPAOperator {

  JPALambdaAnyOperation(OData odata, ServicDocument sd, EntityManager em, List<UriResource> uriResourceParts,
      JPAOperationConverter converter, UriInfoResource member, JPAAbstractQuery root) {
    super(odata, sd, em, uriResourceParts, converter, member, root);
  }

}
