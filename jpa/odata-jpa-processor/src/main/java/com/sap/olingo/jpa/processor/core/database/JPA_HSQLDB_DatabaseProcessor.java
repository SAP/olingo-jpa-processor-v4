package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

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
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPA_HSQLDB_DatabaseProcessor implements JPAODataDatabaseProcessor {
  private final static String SELECT_BASE_PATTERN = "SELECT * FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";
  private final static String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private final static String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> executeFunctionQuery(final UriResourceFunction uriResourceFunction,
      final JPADataBaseFunction jpaFunction, final JPAEntityType returnType, final EntityManager em)
      throws ODataApplicationException {

    final String queryString = generateQueryString(jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, returnType.getTypeClass());
    int count = 1;
    try {
      for (final JPAParameter parameter : jpaFunction.getParameter()) {
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

  private UriParameter findParameterByExternalName(final JPAParameter parameter,
      final List<UriParameter> uriParameters)
      throws ODataApplicationException {
    for (final UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_MISSING,
        HttpStatusCode.BAD_REQUEST, parameter.getName());
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

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final Root<?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);
  }
}
