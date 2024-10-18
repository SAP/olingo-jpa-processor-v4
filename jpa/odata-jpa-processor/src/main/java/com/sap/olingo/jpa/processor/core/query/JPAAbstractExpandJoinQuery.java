package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

abstract class JPAAbstractExpandJoinQuery extends JPAAbstractExpandQuery {

  final Optional<JPAKeyBoundary> keyBoundary;

  JPAAbstractExpandJoinQuery(final OData odata, final JPAEntityType jpaEntityType,
      final JPAODataRequestContextAccess requestContext, final JPAAssociationPath association) throws ODataException {
    super(odata, jpaEntityType, requestContext, association);
    this.keyBoundary = Optional.empty();
  }

  JPAAbstractExpandJoinQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAInlineItemInfo item, final Optional<JPAKeyBoundary> keyBoundary) throws ODataException {
    super(odata, requestContext, item);
    this.keyBoundary = keyBoundary;
  }

  JPAAbstractExpandJoinQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPAEntityType et, final JPAAssociationPath association, final List<JPANavigationPropertyInfo> hops,
      final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {
    super(odata, requestContext, et, association, hops);
    this.keyBoundary = keyBoundary;
  }

  Expression<Boolean> createWhere() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createWhere")) {
      jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
      // Given keys: Organizations('1')/Roles(...)
      whereCondition = createKeyWhere(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createBoundary(navigationInfo, keyBoundary));
      whereCondition = addWhereClause(whereCondition, createExpandWhere());
      whereCondition = addWhereClause(whereCondition, createProtectionWhere(claimsProvider));
      whereCondition = addWhereClause(whereCondition, createWhereEnhancement());
      return whereCondition;
    }
  }

  private jakarta.persistence.criteria.Expression<Boolean> createExpandWhere() throws ODataApplicationException {

    jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
    for (final JPANavigationPropertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        try {
          whereCondition = addWhereClause(whereCondition, info.getFilterCompiler().compile());
        } catch (final ExpressionVisitException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
    }
    return whereCondition;
  }
}
