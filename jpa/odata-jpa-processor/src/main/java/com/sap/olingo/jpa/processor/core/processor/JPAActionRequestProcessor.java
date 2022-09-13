package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ACTION_UNKNOWN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;

public class JPAActionRequestProcessor extends JPAOperationRequestProcessor {

  public JPAActionRequestProcessor(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, requestContext);
  }

  /**
   * Execution of an action. Action selection for overloaded actions, as described in
   * <a href="https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#_Toc31359015">
   * Action Overload Resolution (part1 protocol 11.5.5.2)</a> is done by Olingo. Example:
   * <p>
   * In case an action is defined with binding parameter Business Partner, the action is also available at
   * Organizations. In this case Olingo provides the action with binding parameter Business Partner.
   *
   *
   * @param request
   * @param response
   * @param requestFormat
   * @throws ODataApplicationException
   */
  public void performAction(final ODataRequest request, final ODataResponse response, final ContentType requestFormat)
      throws ODataApplicationException {

    final List<UriResource> resourceList = uriInfo.getUriResourceParts();
    final UriResourceAction resource = (UriResourceAction) resourceList.get(resourceList.size() - 1);
    try {
      final JPAAction jpaAction = sd.getAction(resource.getAction());
      if (jpaAction == null)
        throw new ODataJPAProcessorException(ACTION_UNKNOWN, BAD_REQUEST, resource.getAction().getName());
      final Object instance = createInstance(jpaAction.getConstructor());

      final ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
      final Map<String, org.apache.olingo.commons.api.data.Parameter> actionParameter =
          deserializer.actionParameters(request.getBody(), resource.getAction()).getActionParameters();

      final List<Object> parameter = convertActionParameter(resourceList, resource, jpaAction, actionParameter);
      Annotatable r = null;
      EdmType returnType = null;
      if (resource.getAction().getReturnType() != null) {
        returnType = resource.getAction().getReturnType().getType();
        final Object result = jpaAction.getMethod().invoke(instance, parameter.toArray());
        r = convertResult(result, returnType, jpaAction);
      } else {
        jpaAction.getMethod().invoke(instance, parameter.toArray());
      }
      if (serializer != null)
        serializeResult(returnType, response, serializer.getContentType(), r, request);
      else
        response.setStatusCode(successStatusCode);

    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    } catch (InvocationTargetException | ODataException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof ODataApplicationException) {
        throw (ODataApplicationException) cause;
      } else {
        throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
      }
    }
  }

  private List<Object> convertActionParameter(final List<UriResource> resourceList, final UriResourceAction resource,
      final JPAAction jpaAction, final Map<String, org.apache.olingo.commons.api.data.Parameter> actionParameter)
      throws ODataJPAModelException, ODataApplicationException {

    final List<Object> parameter = new ArrayList<>();
    final Parameter[] methodParameter = jpaAction.getMethod().getParameters();

    for (int i = 0; i < methodParameter.length; i++) {
      final Parameter declaredParameter = methodParameter[i];
      if (i == 0 && resource.getAction().isBound()) {
        parameter.add(createBindingParameter((UriResourceEntitySet) resourceList.get(resourceList.size() - 2))
            .orElse(null));
      } else {
        // Any nullable parameter values not specified in the request MUST be assumed to have the null value.
        // This is guaranteed by Olingo => no code needed
        final String externalName = jpaAction.getParameter(declaredParameter).getName();
        final org.apache.olingo.commons.api.data.Parameter param = actionParameter.get(externalName);
        if (param != null)
          parameter.add(JPAConversionHelper.convertParameter(param, sd));
        else
          parameter.add(null);
      }
    }
    return parameter;
  }

  private Optional<Object> createBindingParameter(final UriResourceEntitySet entitySet) throws ODataJPAModelException,
      ODataApplicationException {

    final JPAEntityType et = sd.getEntity(entitySet.getType());
    if (et != null) {
      return new JPAInstanceCreator<>(odata, et).createInstance(entitySet.getKeyPredicates());
    }
    return Optional.empty();
  }

  protected Object createInstance(final Constructor<?> c) throws InstantiationException, IllegalAccessException,
      InvocationTargetException {
    Object instance;
    if (c.getParameterCount() == 1)
      instance = c.newInstance(em);
    else
      instance = c.newInstance();
    return instance;
  }

}
