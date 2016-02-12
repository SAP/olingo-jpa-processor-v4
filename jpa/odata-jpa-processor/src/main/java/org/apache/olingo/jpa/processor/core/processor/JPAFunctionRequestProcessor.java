package org.apache.olingo.jpa.processor.core.processor;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.jpa.processor.core.query.JPAInstanceResultConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;

/**
 * Functions as User Defined Functions, Native Query, as Criteria Builder does not provide the option to used UDFs in
 * the From clause.
 * @author Oliver Grande
 *
 */
public class JPAFunctionRequestProcessor extends JPAAbstractRequestProcessor {
  private static String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private static String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  public JPAFunctionRequestProcessor(OData odata, ServicDocument sd, EntityManager em,
      UriInfo uriInfo, JPASerializer serializer) {
    super(odata, sd, em, uriInfo, serializer);
  }

  @Override
  public void retrieveData(ODataRequest request, ODataResponse response, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    UriResourceFunction uriResourceFunction = (UriResourceFunction) uriInfo.getUriResourceParts().get(0);
    JPAFunction jpaFunction = sd.getFunction(uriResourceFunction.getFunction());
    List<JPAFunctionParameter> parameterList = jpaFunction.getParameter();
    EdmFunction edmFunction = uriResourceFunction.getFunction();

    String queryString = generateQueryString(jpaFunction);
    JPAEntityType returnType = sd.getEntity(jpaFunction.getReturnType());
    Query nq = em.createNativeQuery(queryString, returnType.getTypeClass());
    int count = 1;
    for (JPAFunctionParameter parameter : parameterList) {
      UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
      Object value = getValue(edmFunction, parameter, uriParameter.getText());
      nq.setParameter(count, value);
      count += 1;
    }
    EntityCollection entityCollection;
    List<?> nr = nq.getResultList();
    EdmEntitySet returnEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
    try {
      entityCollection = new JPAInstanceResultConverter(odata.createUriHelper(), sd, nr, returnEntitySet, returnType
          .getTypeClass()).getResult();
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Result could not be created", HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), Locale.ENGLISH, e);
    } catch (URISyntaxException e) {
      throw new ODataApplicationException("Result could not be created", HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), Locale.ENGLISH, e);
    }

    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
      SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }

  String generateQueryString(JPAFunction jpaFunction) {
    StringBuffer parameterList = new StringBuffer();
    String queryString = SELECT_BASE_PATTERN;

    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
      parameterList.append(",");
      parameterList.append("?");
      parameterList.append(i);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  private UriParameter findParameterByExternalName(JPAFunctionParameter parameter, List<UriParameter> uriParameters)
      throws ODataApplicationException {
    for (UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataApplicationException("Parameter not found " + parameter.getName(), HttpStatusCode.BAD_REQUEST
        .getStatusCode(), Locale.ENGLISH);
  }

  private Object getValue(EdmFunction edmFunction, JPAFunctionParameter parameter, String uriValue)
      throws ODataApplicationException {
    String value = uriValue.replaceAll("'", "");
    EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.maxLength(),
          parameter.precision(), parameter.scale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      throw new ODataApplicationException("Unable to convert parameter value " + uriValue, HttpStatusCode.BAD_REQUEST
          .getStatusCode(), Locale.ENGLISH, e);
    }
  }
}
