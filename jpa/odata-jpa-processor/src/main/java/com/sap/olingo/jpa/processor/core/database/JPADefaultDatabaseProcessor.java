package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
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

public class JPADefaultDatabaseProcessor implements JPAODataDatabaseProcessor, JPAODataDatabaseOperations {
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private static final String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  private CriteriaBuilder cb;

  @Override
  public Expression<Long> convert(final JPAAggregationOperation jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public Expression<Boolean> convert(final JPABooleanOperator jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED);
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
      Long n = ((JPAEnumerationBasedOperator) jpaOperator.getRight()).getValue().longValue();
      @SuppressWarnings("unchecked")
      Expression<Integer> div = cb.quot(jpaOperator.getLeft(), n);
      Expression<Integer> mod = cb.mod(div, 2);
      return cb.equal(mod, 1);

    }
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public <T> Expression<T> convert(final JPAMethodCall jpaFunction) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED, jpaFunction.getName());
  }

  @Override
  public Expression<Boolean> convert(final JPAUnaryBooleanOperator jpaOperator) throws ODataApplicationException {
    throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_OPERATOR,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);

  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em)
      throws ODataApplicationException {

    final String queryString = generateQueryString(jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, jpaFunction.getResultParameter().getType());
    final UriResourceFunction uriResourceFunction =
        (UriResourceFunction) uriResourceParts.get(uriResourceParts.size() - 1);

    int count = 1;
    try {
      if (jpaFunction.isBound()) {
        // TODO Compound key
        final Object value = ((UriResourceEntitySet) uriResourceParts.get(0)).getKeyPredicates().get(0).getText();
        functionQuery.setParameter(count, value);
        count += 1;
      }
      for (int i = count - 1; i < jpaFunction.getParameter().size(); i++) {

        final JPAParameter parameter = jpaFunction.getParameter().get(i);
        final UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
        final Object value = getValue(uriResourceFunction.getFunction(), parameter, uriParameter.getText());
        functionQuery.setParameter(count, value);
        count += 1;
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    return functionQuery.getResultList();
  }

  @Override
  public void setCriterialBuilder(final CriteriaBuilder cb) {
    this.cb = cb;
  }

  protected final UriParameter findParameterByExternalName(final JPAParameter parameter,
      final List<UriParameter> uriParameters) throws ODataApplicationException {
    for (final UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_MISSING,
        HttpStatusCode.BAD_REQUEST, parameter.getName());
  }

  private String generateQueryString(final JPADataBaseFunction jpaFunction) throws ODataJPAProcessorException {

    final StringBuilder parameterList = new StringBuilder();
    String queryString = SELECT_BASE_PATTERN;

    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    try {
      for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
        parameterList.append(',');
        parameterList.append('?');
        parameterList.append(i);
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  private Object getValue(final EdmFunction edmFunction, final JPAParameter parameter, final String uriValue)
      throws ODataApplicationException {
    final String value = uriValue.replaceAll("'", "");
    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.getMaxLength(),
          parameter.getPrecision(), parameter.getScale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      // Unable to convert value %1$s of parameter %2$s
      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
          HttpStatusCode.NOT_IMPLEMENTED, uriValue, parameter.getName());
    }
  }
}
