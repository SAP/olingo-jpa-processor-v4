package org.apache.olingo.jpa.processor.core.processor;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.server.api.OData;

abstract class JPAAbstractGetRequestProcessor extends JPAAbstractRequestProcessor implements JPARequestProcessor {

  public JPAAbstractGetRequestProcessor(OData odata, JPAODataSessionContextAccess context,
      JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, context, requestContext);
  }

}
