package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAComplexResultConverter;
import com.sap.olingo.jpa.processor.core.converter.JPAEntityResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.serializer.JPAFunctionSerializer;

/**
 * Functions as User Defined Functions, Native Query, as Criteria Builder does not provide the option to used UDFs in
 * the From clause.
 * @author Oliver Grande
 *
 */
public final class JPAFunctionRequestProcessor extends JPAAbstractGetRequestProcessor {

  private final JPAODataDatabaseProcessor dbProcessor;

  public JPAFunctionRequestProcessor(final OData odata, final JPAODataSessionContextAccess context,
      final JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, context, requestContext);
    this.dbProcessor = context.getDatabaseProcessor();
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    Object result = null;
    final UriResourceFunction uriResourceFunction = (UriResourceFunction) uriInfo.getUriResourceParts().get(0);
    final JPAFunction jpaFunction = sd.getFunction(uriResourceFunction.getFunction());
    if (jpaFunction.getFunctionType() == EdmFunctionType.JavaClass) {
      result = processJavaFunction(uriResourceFunction, (JPAJavaFunction) jpaFunction, em);

    } else if (jpaFunction.getFunctionType() == EdmFunctionType.UserDefinedFunction)
      result = processJavaUDF(uriResourceFunction, (JPADataBaseFunction) jpaFunction, request, response,
          responseFormat);

    final Annotatable annotatable = convertResult(result, uriResourceFunction, jpaFunction);
    serializeResult(uriResourceFunction, response, responseFormat, annotatable);
  }

  private Annotatable convertResult(final Object result, final UriResourceFunction uriResourceFunction,
      final JPAFunction jpaFunction) throws ODataApplicationException {

    switch (uriResourceFunction.getFunction().getReturnType().getType().getKind()) {
    case PRIMITIVE:
      if (jpaFunction.getResultParameter().isCollection()) {
        final List<Object> response = new ArrayList<Object>();
        response.addAll((Collection<?>) result);
        return new Property(null, "Result", ValueType.COLLECTION_PRIMITIVE, response);
      } else if (result == null)
        return null;
      return new Property(null, "Result", ValueType.PRIMITIVE, result);
    case ENTITY:
      return createEntityCollection(uriResourceFunction, result, odata.createUriHelper(), jpaFunction);
    case COMPLEX:
      if (jpaFunction.getResultParameter().isCollection()) {
        return new Property(null, "Result", ValueType.COLLECTION_COMPLEX, createComplexCollection(uriResourceFunction,
            jpaFunction, result));
      } else if (result == null)
        return null;
      return new Property(null, "Result", ValueType.COMPLEX, createComplexValue(uriResourceFunction, jpaFunction,
          result));
    default:
      break;
    }
    return null;
  }

  private List<ComplexValue> createComplexCollection(final UriResourceFunction uriResourceFunction,
      final JPAFunction jpaFunction, final Object result) throws ODataApplicationException {

    final List<Object> jpaQueryResult = new ArrayList<Object>();
    jpaQueryResult.addAll((Collection<?>) result);
    try {
      return new JPAComplexResultConverter(sd, jpaQueryResult,
          (EdmComplexType) uriResourceFunction.getFunction().getReturnType().getType()).getResult();
    } catch (SerializerException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (URISyntaxException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private ComplexValue createComplexValue(final UriResourceFunction uriResourceFunction, final JPAFunction jpaFunction,
      final Object result) throws ODataApplicationException {

    final List<Object> jpaQueryResult = new ArrayList<Object>();
    jpaQueryResult.add(result);
    try {
      final List<ComplexValue> valueList = new JPAComplexResultConverter(sd, jpaQueryResult,
          (EdmComplexType) uriResourceFunction.getFunction().getReturnType().getType()).getResult();
      return valueList.get(0);
    } catch (SerializerException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (URISyntaxException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private EntityCollection createEntityCollection(final UriResourceFunction uriResourceFunction, Object result,
      UriHelper createUriHelper, final JPAFunction jpaFunction) throws ODataApplicationException {

    final List resultList = new ArrayList();
    if (jpaFunction.getResultParameter().isCollection())
      resultList.addAll((Collection<?>) result);
    else if (result == null)
      return null;
    else
      resultList.add(result);
    final EdmEntitySet returnEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
    try {
      return new JPAEntityResultConverter(odata.createUriHelper(), sd, resultList, returnEntitySet).getResult();
    } catch (SerializerException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (URISyntaxException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private Object getValue(final EdmFunction edmFunction, final JPAParameter parameter, final String uriValue)
      throws ODataApplicationException {
    final String value = uriValue.replaceAll("'", "");
    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, edmParam.getMaxLength(),
          edmParam.getPrecision(), edmParam.getScale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      // Unable to convert value %1$s of parameter %2$s
      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
          HttpStatusCode.NOT_IMPLEMENTED, uriValue, parameter.getName());
    }
  }

  private Object processJavaFunction(final UriResourceFunction uriResourceFunction, final JPAJavaFunction jpaFunction,
      final EntityManager em) throws ODataApplicationException {

    final Constructor<?> c = jpaFunction.getConstructor();

    try {
      Object instance;
      if (c.getParameterCount() == 1)
        instance = c.newInstance(em);
      else
        instance = c.newInstance();
      final List<Object> parameter = new ArrayList<Object>();
      final Parameter[] methodParameter = jpaFunction.getMethod().getParameters();

      for (Parameter declairedParameter : Arrays.asList(methodParameter)) {
        for (UriParameter providedParameter : uriResourceFunction.getParameters()) {
          JPAParameter jpaParameter = jpaFunction.getParameter(declairedParameter.getName());
          if (jpaParameter.getName().equals(providedParameter.getName())) {
            parameter.add(getValue(uriResourceFunction.getFunction(), jpaParameter, providedParameter.getText()));
            break;
          }
        }
      }

      return jpaFunction.getMethod().invoke(instance, parameter.toArray());
    } catch (InstantiationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalAccessException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalArgumentException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (InvocationTargetException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Object processJavaUDF(final UriResourceFunction uriResourceFunction, final JPADataBaseFunction jpaFunction,
      final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws SerializerException, ODataApplicationException {
    JPAEntityType returnType;
    try {
      returnType = sd.getEntity(jpaFunction.getResultParameter().getTypeFQN());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    // dbProcessor.query

    return dbProcessor.executeFunctionQuery(uriResourceFunction, jpaFunction, returnType, em);

//    EntityCollection entityCollection;
//    final EdmEntitySet returnEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
//    try {
//      entityCollection = new JPAInstanceResultConverter(odata.createUriHelper(), sd, nr, returnEntitySet, returnType
//          .getTypeClass()).getResult();
//    } catch (ODataJPAModelException e) {
//      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
//          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
//    } catch (URISyntaxException e) {
//      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_URI_ERROR,
//          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
//    }
//
//    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
//      final SerializerResult serializerResult = serializer.serialize(request, entityCollection);
//      createSuccessResponce(response, responseFormat, serializerResult);
//    } else
//      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  private void serializeResult(final UriResourceFunction uriResourceFunction, final ODataResponse response,
      final ContentType responseFormat, final Annotatable result) throws ODataJPASerializerException,
      SerializerException {

    if (result != null || result instanceof EntityCollection && ((EntityCollection) result).getEntities().size() > 0) {
      final EdmType returnEntityType = uriResourceFunction.getFunction().getReturnType().getType();
      final SerializerResult serializerResult = ((JPAFunctionSerializer) serializer).serialize(result,
          returnEntityType);
      createSuccessResponce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }
}
