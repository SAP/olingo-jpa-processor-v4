package org.apache.olingo.jpa.processor.core.database;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

class JPA_HSQLDB_DatabaseProcessor implements JPAODataDatabaseProcessor {
  private final static String SELECT_BASE_PATTERN = "SELECT * FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";
  private final static String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private final static String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  @Override
  public List<?> executeFunctionQuery(final UriResourceFunction uriResourceFunction, final JPAFunction jpaFunction,
      final JPAEntityType returnType, final EntityManager em) throws ODataApplicationException {

    final String queryString = generateQueryString(jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, returnType.getTypeClass());
    int count = 1;
    for (final JPAFunctionParameter parameter : jpaFunction.getParameter()) {
      final UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
      final Object value = getValue(uriResourceFunction.getFunction(), parameter, uriParameter.getText());
      functionQuery.setParameter(count, value);
      count += 1;
    }
    return functionQuery.getResultList();
  }

  private String generateQueryString(final JPAFunction jpaFunction) {
    final StringBuffer parameterList = new StringBuffer();
    String queryString = SELECT_BASE_PATTERN;

    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
      parameterList.append(',');
      parameterList.append('?');
      parameterList.append(i);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  private UriParameter findParameterByExternalName(final JPAFunctionParameter parameter,
      final List<UriParameter> uriParameters)
          throws ODataApplicationException {
    for (final UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataApplicationException("Parameter not found " + parameter.getName(), HttpStatusCode.BAD_REQUEST
        .getStatusCode(), Locale.ENGLISH);
  }

  private Object getValue(final EdmFunction edmFunction, final JPAFunctionParameter parameter, final String uriValue)
      throws ODataApplicationException {
    final String value = uriValue.replaceAll("'", "");
    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.maxLength(),
          parameter.precision(), parameter.scale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      throw new ODataApplicationException("Unable to convert parameter value " + uriValue, HttpStatusCode.BAD_REQUEST
          .getStatusCode(), Locale.ENGLISH, e);
    }
  }

  @Override
  public Expression<Boolean> createSearchWhereClause(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<?> root,
      JPAEntityType entityType, SearchOption searchOption) throws ODataApplicationException {
    throw new ODataApplicationException("Search not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }
}
