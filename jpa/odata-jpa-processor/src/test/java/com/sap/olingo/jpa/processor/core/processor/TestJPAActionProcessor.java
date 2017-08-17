package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.serializer.JPAOperationSerializer;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testobjects.TestJavaActionNoParameter;
import com.sap.olingo.jpa.processor.core.testobjects.TestJavaActions;

public class TestJPAActionProcessor {
  private JPAActionRequestProcessor cut;
  private ContentType requestFormat;
  private ContentType responseFormat;
  @Mock
  private ODataRequest request;
  @Mock
  private ODataResponse response;
  @Mock
  private OData odata;
  @Mock
  private ODataDeserializer deserializer;
  @Mock
  private JPAOperationSerializer serializer;
  @Mock
  private JPAODataSessionContextAccess sessionContext;
  @Mock
  private JPAODataRequestContextAccess requestContext;
  @Mock
  private JPAServiceDocument sd;
  @Mock
  private UriInfo uriInfo;
  private List<UriResource> uriResources;
  @Mock
  private UriResourceAction resource;
  @Mock
  private EdmAction edmAction;
  @Mock
  private JPAAction action;
  private Map<String, Parameter> actionParameter;
  @Mock
  private CsdlReturnType returnType;

  @Before
  public void setup() throws ODataException {
    MockitoAnnotations.initMocks(this);

    uriResources = new ArrayList<UriResource>();
    uriResources.add(resource);
    actionParameter = new HashMap<String, Parameter>();

    EntityManager em = mock(EntityManager.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    JPAEdmProvider edmProvider = mock(JPAEdmProvider.class);
    DeserializerResult dResult = mock(DeserializerResult.class);
    SerializerResult serializerResult = mock(SerializerResult.class);

    when(requestContext.getEntityManager()).thenReturn(em);
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    when(edmProvider.getServiceDocument()).thenReturn(sd);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(serializer.serialize(any(Annotatable.class), any(EdmType.class))).thenReturn(serializerResult);

    when(uriInfo.getUriResourceParts()).thenReturn(uriResources);
    when(resource.getAction()).thenReturn(edmAction);
    when(sd.getAction(edmAction)).thenReturn(action);
    when(odata.createDeserializer((ContentType) any())).thenReturn(deserializer);

    when(deserializer.actionParameters(request.getBody(), resource.getAction())).thenReturn(dResult);
    when(dResult.getActionParameters()).thenReturn(actionParameter);

    requestFormat = ContentType.APPLICATION_JSON;
    responseFormat = ContentType.APPLICATION_JSON;

    cut = new JPAActionRequestProcessor(odata, sessionContext, requestContext);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsConstructorWithoutParemeter() throws ODataJPAProcessException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    TestJavaActionNoParameter.resetCalls();

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActionNoParameter.class.getConstructors()[0];
    Method m = TestJavaActionNoParameter.class.getMethod("unboundReturnPrimitivetNoParameter");
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    when(action.getReturnType()).thenReturn(null);
    cut.performAction(request, response, requestFormat, responseFormat);

    assertEquals(1, TestJavaActionNoParameter.constructorCalls);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsConstructorWithParemeter() throws ODataJPAProcessException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    TestJavaActions.constructorCalls = 0;

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActions.class.getConstructors()[0];
    Method m = TestJavaActions.class.getMethod("unboundWithOutParameter");
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    when(action.getReturnType()).thenReturn(null);
    cut.performAction(request, response, requestFormat, responseFormat);

    assertEquals(1, TestJavaActions.constructorCalls);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsActionVoidNoParameterReturnNoContent() throws ODataJPAProcessException, NoSuchMethodException,
      SecurityException {

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActions.class.getConstructors()[0];
    Method m = TestJavaActions.class.getMethod("unboundWithOutParameter");
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    when(action.getReturnType()).thenReturn(null);

    cut.performAction(request, response, requestFormat, responseFormat);
    verify(response, times(1)).setStatusCode(204);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsActionPrimitiveNoParameterReturnValue() throws ODataJPAProcessException, NoSuchMethodException,
      SecurityException, SerializerException {

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActions.class.getConstructors()[0];
    Method m = TestJavaActions.class.getMethod("unboundReturnFacetNoParameter");
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    final JPAOperationResultParameter rParam = mock(JPAOperationResultParameter.class);
    when(action.getResultParameter()).thenReturn(rParam);

    EdmReturnType rt = mock(EdmReturnType.class);
    EdmType type = mock(EdmType.class);
    when(edmAction.getReturnType()).thenReturn(rt);
    when(rt.getType()).thenReturn(type);
    when(type.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);

    cut.performAction(request, response, requestFormat, responseFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(serializer, times(1)).serialize(any(Annotatable.class), eq(type));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsActionEntityNoParameterReturnValue() throws ODataJPAProcessException, NoSuchMethodException,
      SecurityException, SerializerException {

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActions.class.getConstructors()[0];
    Method m = TestJavaActions.class.getMethod("returnEmbeddable");
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    final JPAOperationResultParameter rParam = mock(JPAOperationResultParameter.class);
    when(action.getResultParameter()).thenReturn(rParam);

    EdmReturnType rt = mock(EdmReturnType.class);
    EdmComplexType type = mock(EdmComplexType.class);
    when(edmAction.getReturnType()).thenReturn(rt);
    when(rt.getType()).thenReturn(type);
    when(type.getKind()).thenReturn(EdmTypeKind.COMPLEX);

    JPAStructuredType st = mock(JPAStructuredType.class);
    when(sd.getComplexType((EdmComplexType) any())).thenReturn(st);
    when(st.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return CommunicationData.class;
      }
    });

    cut.performAction(request, response, requestFormat, responseFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(serializer, times(1)).serialize(any(Annotatable.class), eq(type));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsActionVoidOneParameterReturnNoContent() throws ODataJPAProcessException, NoSuchMethodException,
      SecurityException {
    TestJavaActionNoParameter.resetCalls();

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActionNoParameter.class.getConstructors()[0];
    Method m = TestJavaActionNoParameter.class.getMethod("unboundVoidOneParameter", Short.class);
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    when(action.getReturnType()).thenReturn(null);

    addParameter(m, new Short("10"), "A", 0);

    cut.performAction(request, response, requestFormat, responseFormat);
    verify(response, times(1)).setStatusCode(204);
    assertEquals(new Short((short) 10), TestJavaActionNoParameter.param1);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCallsActionVoidTwoParameterReturnNoContent() throws ODataJPAProcessException, NoSuchMethodException,
      SecurityException {
    TestJavaActionNoParameter.resetCalls();

    @SuppressWarnings("rawtypes")
    Constructor c = TestJavaActionNoParameter.class.getConstructors()[0];
    Method m = TestJavaActionNoParameter.class.getMethod("unboundVoidTwoParameter", Short.class, Integer.class);
    when(action.getConstructor()).thenReturn(c);
    when(action.getMethod()).thenReturn(m);
    when(action.getReturnType()).thenReturn(null);

    addParameter(m, new Short("10"), "A", 0);
    addParameter(m, new Integer("200000"), "B", 1);

    cut.performAction(request, response, requestFormat, responseFormat);
    verify(response, times(1)).setStatusCode(204);
    assertEquals(new Short((short) 10), TestJavaActionNoParameter.param1);
  }

  private void addParameter(final Method m, final Object value, final String name, int index) {
    Parameter param = mock(Parameter.class);
    when(param.getValue()).thenReturn(value);
    when(param.getName()).thenReturn(name);
    actionParameter.put(name, param);

    JPAParameter jpaParam = mock(JPAParameter.class);
    when(action.getParameter(m.getParameters()[index])).thenReturn(jpaParam);
    when(jpaParam.getName()).thenReturn(name);
  }

}
