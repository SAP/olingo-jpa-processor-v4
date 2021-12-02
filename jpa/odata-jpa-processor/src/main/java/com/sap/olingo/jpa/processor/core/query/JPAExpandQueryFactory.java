package com.sap.olingo.jpa.processor.core.query;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;

import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;

public class JPAExpandQueryFactory {
  private final OData odata;
  private final JPAODataSessionContextAccess sessionContext;
  private final JPAODataRequestContextAccess requestContext;
  private final CriteriaBuilder cb;

  public JPAExpandQueryFactory(final OData odata, final JPAODataSessionContextAccess sessionContext,
      final JPAODataRequestContextAccess requestContext, final CriteriaBuilder cb) {
    this.odata = odata;
    this.sessionContext = sessionContext;
    this.requestContext = requestContext;
    this.cb = cb;
  }

  public JPAAbstractExpandQuery createQuery(final JPAExpandItemInfo item, final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {
    if (cb instanceof ProcessorCriteriaBuilder)
      return new JPAExpandSubQuery(odata, sessionContext, item, requestContext);
    return new JPAExpandJoinQuery(odata, sessionContext, item, requestContext, keyBoundary);
  }
}
