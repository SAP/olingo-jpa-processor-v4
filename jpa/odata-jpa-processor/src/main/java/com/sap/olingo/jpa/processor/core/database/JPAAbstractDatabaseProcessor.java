package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public abstract class JPAAbstractDatabaseProcessor implements JPAODataDatabaseProcessor {
  static final String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  static final String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  protected UriResourceEntitySet determineTargetEntitySet(final List<UriResource> uriParts) {
    for (int i = uriParts.size() - 1; i >= 0; i--) {
      if (uriParts.get(i).getKind() == UriResourceKind.entitySet)
        return (UriResourceEntitySet) uriParts.get(i);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected <T> List<T> executeQuery(final List<UriResource> uriParts, final JPADataBaseFunction jpaFunction,
      final EntityManager em, final String pattern)
      throws ODataApplicationException {

    final UriResourceFunction uriFunction = (UriResourceFunction) uriParts.get(uriParts.size() - 1);
    final String queryString = generateQueryString(pattern, jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, jpaFunction.getResultParameter().getType());
    fillParameter(determineTargetEntitySet(uriParts), jpaFunction, uriFunction, functionQuery);
    return functionQuery.getResultList();
  }

  protected void fillParameter(final UriResourceEntitySet es, final JPADataBaseFunction jpaFunction,
      final UriResourceFunction uriResourceFunction, final Query functionQuery)
      throws ODataApplicationException {

    try {
      if (jpaFunction.isBound() && uriResourceFunction.getParameters().isEmpty())
        fillParameterFromEntity(jpaFunction, es, functionQuery);
      else
        fillParameterFromFunction(jpaFunction, uriResourceFunction, functionQuery);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  protected String generateQueryString(final String queryPattern, final JPADataBaseFunction jpaFunction)
      throws ODataJPAProcessorException {

    final StringBuilder parameterList = new StringBuilder();

    final String queryString = queryPattern.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    try {
      for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
        parameterList.append(',');
        parameterList.append('?');
        parameterList.append(i);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  protected UriParameter findParameterByExternalName(final JPAParameter parameter,
      final List<UriParameter> uriParameters) throws ODataApplicationException {

    for (final UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_MISSING,
        HttpStatusCode.BAD_REQUEST, parameter.getName());
  }

  protected void fillParameterFromEntity(final JPADataBaseFunction jpaFunction, final UriResourceEntitySet es,
      final Query functionQuery) throws ODataApplicationException {

    int count = 1;
    try {
      for (final JPAParameter parameter : jpaFunction.getParameter()) {
        final UriParameter uriParameter = findParameterByExternalName(parameter, es.getKeyPredicates());
        final Object value = getValue(es.getEntityType().getProperty(parameter.getName()), parameter, uriParameter
            .getText());
        functionQuery.setParameter(count, value);
        count += 1;
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  protected Long executeCountQuery(final List<UriResource> uriParts, final JPADataBaseFunction jpaFunction,
      final EntityManager em, final String pattern) throws ODataApplicationException {

    final UriResourceFunction uriFunction = (UriResourceFunction) uriParts.get(uriParts.size() - 2);
    final String queryString = generateQueryString(pattern, jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString);

    fillParameter(determineTargetEntitySet(uriParts), jpaFunction, uriFunction, functionQuery);
    return (Long) functionQuery.getSingleResult();

  }

  protected void fillParameterFromFunction(final JPADataBaseFunction jpaFunction,
      final UriResourceFunction uriResourceFunction, final Query functionQuery) throws ODataApplicationException {

    final EdmFunction edmFunction = uriResourceFunction.getFunction();
    int count = 1;
    try {
      for (final JPAParameter parameter : jpaFunction.getParameter()) {
        final UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
        final Object value = getValue(edmFunction.getParameter(parameter.getName()), parameter, uriParameter.getText());
        functionQuery.setParameter(count, value);
        count += 1;
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Object getValue(final EdmElement edmElement, final JPAParameter parameter, final String uriValue)
      throws ODataApplicationException {

    final String value = uriValue.replace("'", "");
    try {
      return ((EdmPrimitiveType) edmElement.getType()).valueOfString(value, false, parameter.getMaxLength(),
          parameter.getPrecision(), parameter.getScale(), true, parameter.getType());
    } catch (final EdmPrimitiveTypeException e) {
      // Unable to convert value %1$s of parameter %2$s
      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, uriValue, parameter.getName());
    }
  }
}
