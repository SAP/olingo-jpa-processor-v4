package com.sap.olingo.jpa.processor.core.database;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_FUNC_WITH_NAVI;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.filter.JPAAggregationOperation;
import com.sap.olingo.jpa.processor.core.filter.JPAArithmeticOperator;
import com.sap.olingo.jpa.processor.core.filter.JPABooleanOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAComparisonOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAEnumerationBasedOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAMethodCall;
import com.sap.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator;

public class JPADefaultDatabaseProcessor extends JPAAbstractDatabaseProcessor implements JPAODataDatabaseOperations {
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM $FUNCTIONNAME$($PARAMETER$)";

  private CriteriaBuilder cb;

  @Override
  public Expression<Long> convert(final JPAAggregationOperation jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaOperator.getName());
  }

  @Override
  public <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaOperator.getName());
  }

  @Override
  public Expression<Boolean> convert(final JPABooleanOperator jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaOperator.getName());
  }

  @Override
  public Expression<Boolean> convert(@SuppressWarnings("rawtypes") final JPAComparisonOperator jpaOperator)
      throws ODataApplicationException {
    if (jpaOperator.getOperator().equals(BinaryOperatorKind.HAS)) {
      /*
       * HAS requires an bitwise AND. This is not part of SQL and so not part of the criterion builder. Different
       * databases have different ways to support this. One group uses a function, which is called BITAND e.g. H2,
       * HSQLDB, SAP HANA, DB2 or ORACLE, others have created an operator '&' like PostgesSQL or MySQL.
       * To provide a unique, but slightly slower, solution a workaround is used, see
       * https://stackoverflow.com/questions/20570481/jpa-oracle-bit-operations-using-criteriabuilder#25508741
       */
      final Number n = ((JPAEnumerationBasedOperator) jpaOperator.getRight()).getValue();
      @SuppressWarnings("unchecked")
      final Expression<Integer> div = cb.quot(jpaOperator.getLeft(), n);
      final Expression<Integer> mod = cb.mod(div, 2);
      return cb.equal(mod, 1);

    }
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaOperator.getName());
  }

  @Override
  public <T> Expression<T> convert(final JPAMethodCall jpaFunction) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaFunction.getName());
  }

  @Override
  public Expression<Boolean> convert(final JPAUnaryBooleanOperator jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaOperator.getName());
  }

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);

  }

  @Override
  public Object executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em, final JPAHttpHeaderMap headers,
      final JPARequestParameterMap parameters) throws ODataApplicationException {
    final UriResource last = uriResourceParts.get(uriResourceParts.size() - 1);

    if (last.getKind() == UriResourceKind.count) {
      final List<Long> countResult = new ArrayList<>();
      countResult.add(executeCountQuery(uriResourceParts, jpaFunction, em, SELECT_COUNT_PATTERN));
      return countResult;
    }
    if (last.getKind() == UriResourceKind.function)
      return executeQuery(uriResourceParts, jpaFunction, em, SELECT_BASE_PATTERN);
    throw new ODataJPAProcessorException(NOT_SUPPORTED_FUNC_WITH_NAVI, HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void setCriteriaBuilder(final CriteriaBuilder cb) {
    this.cb = cb;
  }
}
