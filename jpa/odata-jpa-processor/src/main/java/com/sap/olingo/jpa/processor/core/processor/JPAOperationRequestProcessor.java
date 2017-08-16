package com.sap.olingo.jpa.processor.core.processor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAComplexResultConverter;
import com.sap.olingo.jpa.processor.core.converter.JPAEntityResultConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.serializer.JPAOperationSerializer;

abstract class JPAOperationRequestProcessor extends JPAAbstractRequestProcessor {

  public JPAOperationRequestProcessor(OData odata, JPAODataSessionContextAccess context,
      JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, context, requestContext);
  }

  protected Annotatable convertResult(final Object result, final EdmType returnType,
      final JPAOperation jpaOperation) throws ODataApplicationException {

    switch (returnType.getKind()) {
    case PRIMITIVE:
      if (jpaOperation.getResultParameter().isCollection()) {
        final List<Object> response = new ArrayList<Object>();
        response.addAll((Collection<?>) result);
        return new Property(null, "Result", ValueType.COLLECTION_PRIMITIVE, response);
      } else if (result == null)
        return null;
      return new Property(null, "Result", ValueType.PRIMITIVE, result);
    case ENTITY:
      return createEntityCollection((EdmEntityType) returnType, result, odata.createUriHelper(), jpaOperation);
    case COMPLEX:
      if (jpaOperation.getResultParameter().isCollection()) {
        return new Property(null, "Result", ValueType.COLLECTION_COMPLEX, createComplexCollection(
            (EdmComplexType) returnType, jpaOperation, result));
      } else if (result == null)
        return null;
      return new Property(null, "Result", ValueType.COMPLEX, createComplexValue((EdmComplexType) returnType,
          jpaOperation, result));
    default:
      break;
    }
    return null;
  }

  private List<ComplexValue> createComplexCollection(final EdmComplexType returnType, final JPAOperation jpaFunction,
      final Object result) throws ODataApplicationException {

    final List<Object> jpaQueryResult = new ArrayList<Object>();
    jpaQueryResult.addAll((Collection<?>) result);
    try {
      return new JPAComplexResultConverter(sd, jpaQueryResult, returnType).getResult();
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

  private ComplexValue createComplexValue(final EdmComplexType returnType, final JPAOperation jpaFunction,
      final Object result) throws ODataApplicationException {

    final List<Object> jpaQueryResult = new ArrayList<Object>();
    jpaQueryResult.add(result);
    try {
      final List<ComplexValue> valueList = new JPAComplexResultConverter(sd, jpaQueryResult, returnType).getResult();
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
  private EntityCollection createEntityCollection(final EdmEntityType returnType, Object result,
      UriHelper createUriHelper, final JPAOperation jpaFunction)
      throws ODataApplicationException {

    final List resultList = new ArrayList();
    if (jpaFunction.getResultParameter().isCollection())
      resultList.addAll((Collection<?>) result);
    else if (result == null)
      return null;
    else
      resultList.add(result);
    // final EdmEntitySet returnEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
    try {
      return new JPAEntityResultConverter(createUriHelper, sd, resultList, returnType).getResult();
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

  protected void serializeResult(final EdmType returnType, final ODataResponse response,
      final ContentType responseFormat, final Annotatable result)
      throws ODataJPASerializerException, SerializerException {

    if (result != null || result instanceof EntityCollection && ((EntityCollection) result).getEntities().size() > 0) {
      final SerializerResult serializerResult = ((JPAOperationSerializer) serializer).serialize(result, returnType);
      createSuccessResponce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

}
