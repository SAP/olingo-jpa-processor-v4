package com.sap.olingo.jpa.processor.core.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.uri.UriResourceAction;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public class JPAActionRequestProcessor extends JPAOperationRequestProcessor {

  public JPAActionRequestProcessor(final OData odata, final JPAODataSessionContextAccess context,
      final JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, context, requestContext);
  }

  public void performAction(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataJPAProcessException {
    UriResourceAction resource = (UriResourceAction) uriInfo.getUriResourceParts().get(0);
    try {
      JPAAction jpaAction = sessionContext.getEdmProvider().getServiceDocument().getAction(resource.getAction());
      final Constructor<?> c = jpaAction.getConstructor();

      try {
        Object instance;
        if (c.getParameterCount() == 1)
          instance = c.newInstance(em);
        else
          instance = c.newInstance();

        final List<Object> parameter = new ArrayList<Object>();
        final Parameter[] methodParameter = jpaAction.getMethod().getParameters();

        final ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        final Map<String, org.apache.olingo.commons.api.data.Parameter> actionParameter =
            deserializer.actionParameters(request.getBody(), resource.getAction()).getActionParameters();

        for (Parameter declairedParameter : Arrays.asList(methodParameter)) {
          jpaAction.getParameter(declairedParameter).getName();
//          for (UriParameter providedParameter : resource.ggetParameters()) {
//            JPAParameter jpaParameter = jpaAction.getParameter(declairedParameter.getName());
//            if (jpaParameter.getName().equals(providedParameter.getName())) {
//              parameter.add(getValue(resource.getAction(), jpaParameter, providedParameter.getText()));
//              break;
//            }
//          }
        }
        final EdmType returnType = resource.getAction().getReturnType().getType();
        final Object result = jpaAction.getMethod().invoke(instance, parameter.toArray());
        final Annotatable r = convertResult(result, returnType, jpaAction);
        serializeResult(returnType, response, responseFormat, r);

//       response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      } catch (InstantiationException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      } catch (IllegalAccessException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      } catch (IllegalArgumentException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof ODataApplicationException) {
          throw (ODataApplicationException) cause;
        } else {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    } catch (ODataException e) {
      Throwable cause = e.getCause();
      if (cause != null && cause instanceof ODataApplicationException) {
        throw (ODataJPAProcessException) cause;
      } else {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }

  }

}
