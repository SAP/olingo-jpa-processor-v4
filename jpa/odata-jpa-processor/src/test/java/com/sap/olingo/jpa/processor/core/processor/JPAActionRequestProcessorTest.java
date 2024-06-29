package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.serializer.JPAOperationSerializer;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testobjects.FileAccess;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionActionConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.TestJavaActionNoParameter;
import com.sap.olingo.jpa.processor.core.testobjects.TestJavaActions;

class JPAActionRequestProcessorTest {

  private JPAActionRequestProcessor cut;
  private ContentType requestFormat;
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
  @Mock
  private UriResourceEntitySet bindingEntity;
  @Mock
  private UriHelper uriHelper;
  @Mock
  private JPAODataEtagHelper etagHelper;

  @BeforeEach
  void setup() throws ODataException {
    MockitoAnnotations.openMocks(this);

    uriResources = new ArrayList<>();
    uriResources.add(resource);
    actionParameter = new HashMap<>();

    final EntityManager em = mock(EntityManager.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    final JPAEdmProvider edmProvider = mock(JPAEdmProvider.class);
    final DeserializerResult dResult = mock(DeserializerResult.class);
    final SerializerResult serializerResult = mock(SerializerResult.class);

    when(requestContext.getEntityManager()).thenReturn(em);
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);
    when(edmProvider.getServiceDocument()).thenReturn(sd);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(requestContext.getRequestParameter()).thenReturn(new JPARequestParameterHashMap());
    when(requestContext.getHeader()).thenReturn(new JPAHttpHeaderHashMap());
    when(requestContext.getEtagHelper()).thenReturn(etagHelper);
    when(serializer.serialize(any(Annotatable.class), any(EdmType.class), any(ODataRequest.class)))
        .thenReturn(serializerResult);
    when(serializer.getContentType()).thenReturn(ContentType.APPLICATION_JSON);

    when(uriInfo.getUriResourceParts()).thenReturn(uriResources);
    when(resource.getAction()).thenReturn(edmAction);
    when(edmAction.isBound()).thenReturn(Boolean.FALSE);
    when(sd.getAction(edmAction)).thenReturn(action);
    when(odata.createDeserializer((ContentType) any())).thenReturn(deserializer);
    when(odata.createUriHelper()).thenReturn(uriHelper);

    when(deserializer.actionParameters(request.getBody(), resource.getAction())).thenReturn(dResult);
    when(dResult.getActionParameters()).thenReturn(actionParameter);

    requestFormat = ContentType.APPLICATION_JSON;

    cut = new JPAActionRequestProcessor(odata, requestContext);
  }

  @Test
  void testCallsConstructorWithoutParameter() throws NoSuchMethodException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    setConstructorAndMethod("unboundReturnPrimitiveNoParameter");

    cut.performAction(request, response, requestFormat);

    assertEquals(1, TestJavaActionNoParameter.constructorCalls);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCallsConstructorWithParameter() throws NoSuchMethodException, SecurityException, ODataApplicationException {
    TestJavaActions.constructorCalls = 0;

    @SuppressWarnings("rawtypes")
    final Constructor constructor = TestJavaActions.class.getConstructors()[0];
    final Method method = TestJavaActions.class.getMethod("unboundWithOutParameter");
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    when(action.getReturnType()).thenReturn(null);
    cut.performAction(request, response, requestFormat);

    assertEquals(1, TestJavaActions.constructorCalls);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCallsActionVoidNoParameterReturnNoContent() throws NoSuchMethodException, SecurityException,
      ODataApplicationException {

    @SuppressWarnings("rawtypes")
    final Constructor constructor = TestJavaActions.class.getConstructors()[0];
    final Method method = TestJavaActions.class.getMethod("unboundWithOutParameter");
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    when(action.getReturnType()).thenReturn(null);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCallsActionPrimitiveNoParameterReturnValue() throws NoSuchMethodException, SecurityException,
      SerializerException, ODataApplicationException {

    @SuppressWarnings("rawtypes")
    final Constructor constructor = TestJavaActions.class.getConstructors()[0];
    final Method method = TestJavaActions.class.getMethod("unboundReturnFacetNoParameter");
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    final JPAOperationResultParameter rParam = mock(JPAOperationResultParameter.class);
    when(action.getResultParameter()).thenReturn(rParam);

    final EdmReturnType rt = mock(EdmReturnType.class);
    final EdmType type = mock(EdmType.class);
    when(edmAction.getReturnType()).thenReturn(rt);
    when(rt.getType()).thenReturn(type);
    when(type.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(serializer, times(1)).serialize(any(Annotatable.class), eq(type), any(ODataRequest.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCallsActionEntityNoParameterReturnValue() throws NoSuchMethodException,
      SecurityException, SerializerException, ODataApplicationException {

    @SuppressWarnings("rawtypes")
    final Constructor constructor = TestJavaActions.class.getConstructors()[0];
    final Method method = TestJavaActions.class.getMethod("returnEmbeddable");
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    final JPAOperationResultParameter rParam = mock(JPAOperationResultParameter.class);
    when(action.getResultParameter()).thenReturn(rParam);

    final EdmReturnType rt = mock(EdmReturnType.class);
    final EdmComplexType type = mock(EdmComplexType.class);
    when(edmAction.getReturnType()).thenReturn(rt);
    when(rt.getType()).thenReturn(type);
    when(type.getKind()).thenReturn(EdmTypeKind.COMPLEX);

    final JPAStructuredType st = mock(JPAStructuredType.class);
    when(sd.getComplexType((EdmComplexType) any())).thenReturn(st);
    doReturn(CommunicationData.class).when(st).getTypeClass();

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(serializer, times(1)).serialize(any(Annotatable.class), eq(type), any(ODataRequest.class));
  }

  @Test
  void testCallsActionVoidOneParameterReturnNoContent() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("unboundVoidOneParameter", Short.class);

    addParameter(method, Short.valueOf("10"), "A", 0);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertEquals((short) 10, TestJavaActionNoParameter.param1);
  }

  @Test
  void testCallsActionVoidOneEnumerationParameterReturnNoContent() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, ODataApplicationException {

    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("unboundVoidOneEnumerationParameter", FileAccess.class);

    addParameter(method, FileAccess.Create, "AccessRights", 0);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertEquals(FileAccess.Create, TestJavaActionNoParameter.enumeration);
  }

  @Test
  void testCallsActionVoidTwoParameterReturnNoContent() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("unboundVoidTwoParameter", Short.class, Integer.class);

    addParameter(method, Short.valueOf("10"), "A", 0);
    addParameter(method, Integer.valueOf("200000"), "B", 1);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertEquals((short) 10, TestJavaActionNoParameter.param1);
  }

  @Test
  void testCallsActionVoidOneParameterNullableGivenNullReturnNoContent() throws NoSuchMethodException,
      SecurityException, ODataJPAModelException, NumberFormatException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("unboundVoidOneParameter", Short.class);

    addParameter(method, null, "A", 0);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertNull(TestJavaActionNoParameter.param1);
  }

  @Test
  void testCallsActionVoidOnlyBindingParameter() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, EdmPrimitiveTypeException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("boundOnlyBinding", AdministrativeDivision.class);
    when(edmAction.isBound()).thenReturn(Boolean.TRUE);
    uriResources.add(0, bindingEntity);

    setBindingParameter(method);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertNotNull(TestJavaActionNoParameter.bindingParam);
    assertEquals("LAU2", TestJavaActionNoParameter.bindingParam.getCodeID());
  }

  @Test
  void testCallsActionVoidBindingParameterPlusTwoBothNull() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, EdmPrimitiveTypeException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("boundBindingPlus", AdministrativeDivision.class, Short.class,
        Integer.class);
    when(edmAction.isBound()).thenReturn(Boolean.TRUE);
    uriResources.add(0, bindingEntity);

    setBindingParameter(method);

    JPAParameter jpaParam = mock(JPAParameter.class);
    when(action.getParameter(method.getParameters()[1])).thenReturn(jpaParam);
    when(jpaParam.getName()).thenReturn("A");

    jpaParam = mock(JPAParameter.class);
    when(action.getParameter(method.getParameters()[2])).thenReturn(jpaParam);
    when(jpaParam.getName()).thenReturn("B");

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertNotNull(TestJavaActionNoParameter.bindingParam);
    assertEquals("LAU2", TestJavaActionNoParameter.bindingParam.getCodeID());
    assertNull(TestJavaActionNoParameter.param1);
    assertNull(TestJavaActionNoParameter.param2);
  }

  @Test
  void testCallsActionVoidBindingParameterPlusTwoFirstNull() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, EdmPrimitiveTypeException, ODataApplicationException {
    TestJavaActionNoParameter.resetCalls();

    final Method method = setConstructorAndMethod("boundBindingPlus", AdministrativeDivision.class, Short.class,
        Integer.class);
    when(edmAction.isBound()).thenReturn(Boolean.TRUE);
    uriResources.add(0, bindingEntity);

    setBindingParameter(method);

    final JPAParameter jpaParam = mock(JPAParameter.class);
    when(action.getParameter(method.getParameters()[1])).thenReturn(jpaParam);
    when(jpaParam.getName()).thenReturn("A");

    addParameter(method, 20, "B", 2);

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
    assertNotNull(TestJavaActionNoParameter.bindingParam);
    assertEquals("LAU2", TestJavaActionNoParameter.bindingParam.getCodeID());
    assertNull(TestJavaActionNoParameter.param1);
    assertEquals(20, TestJavaActionNoParameter.param2);
  }

  @Test
  void testCallsActionVoidBindingParameterAbstractCallBySubtype() throws NoSuchMethodException, SecurityException,
      ODataJPAModelException, NumberFormatException, ODataApplicationException {

    TestJavaActionNoParameter.resetCalls();
    final Method method = setConstructorAndMethod("boundBindingSuperType", BusinessPartner.class);
    when(edmAction.isBound()).thenReturn(Boolean.TRUE);
    uriResources.add(0, bindingEntity);

    final JPAParameter bindingParam = addParameter(method, null, "Root", 0);
    doReturn(BusinessPartner.class).when(bindingParam).getType();
    final JPAEntityType et = mock(JPAEntityType.class);
    when(sd.getEntity((EdmType) any())).thenReturn(et);
    doReturn(Organization.class).when(et).getTypeClass();
    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
  }

  @Test
  void testCallsWithConstructorParameterValue() throws NoSuchMethodException, ODataJPAModelException,
      ODataApplicationException {
    final Method method = setConstructorAndMethod(TestFunctionActionConstructor.class, "action", LocalDate.class);
    addParameter(method, LocalDate.now(), "date", 0);
    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(204);
  }

  @Test
  void testEtagHeaderFilledForEntity() throws NoSuchMethodException, ODataApplicationException, ODataJPAModelException,
      SerializerException {
    setConstructorAndMethod(TestJavaActions.class, "returnsEntityWithETag");

    final EdmReturnType edmReturnType = mock(EdmReturnType.class);
    final EdmEntityType type = mock(EdmEntityType.class);
    when(edmAction.getReturnType()).thenReturn(edmReturnType);
    when(edmReturnType.getType()).thenReturn(type);
    when(type.getKind()).thenReturn(EdmTypeKind.ENTITY);

    final JPAOperationResultParameter resultParameter = mock(JPAOperationResultParameter.class);
    when(action.getResultParameter()).thenReturn(resultParameter);
    when(resultParameter.isCollection()).thenReturn(false);

    final JPAEntityType jpaType = mock(JPAEntityType.class);
    final JPAAttribute idAttribute = createJPAAttribute("iD", "Test", "ID");
    final JPAAttribute etagAttribute = createJPAAttribute("eTag", "Test", "ETag");
    final List<JPAAttribute> attributes = Arrays.asList(idAttribute, etagAttribute);
    final JPAPath etagPath = mock(JPAPath.class);
    final JPAElement pathPart = mock(JPAElement.class);
    when(sd.getEntity(type)).thenReturn(jpaType);
    when(jpaType.getExternalFQN()).thenReturn(new FullQualifiedName("Test", "Person"));
    doReturn(Person.class).when(jpaType).getTypeClass();
    when(jpaType.getAttributes()).thenReturn(attributes);
    when(jpaType.hasEtag()).thenReturn(true);
    when(jpaType.getEtagPath()).thenReturn(etagPath);
    when(etagPath.getPath()).thenReturn(Arrays.asList(pathPart));
    when(pathPart.getInternalName()).thenReturn("eTag");
    when(etagHelper.asEtag(any(), any())).thenReturn("\"7\"");

    when(uriHelper.buildKeyPredicate(any(), any())).thenReturn("example.org");

    cut.performAction(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(response, times(1)).setHeader(HttpHeader.ETAG, "\"7\"");
  }

  private JPAAttribute createJPAAttribute(final String internalName, final String namespace,
      final String externalName) {
    final JPAAttribute attribute = mock(JPAAttribute.class);
    when(attribute.getInternalName()).thenReturn(internalName);
    when(attribute.getExternalName()).thenReturn(externalName);
    when(attribute.getExternalFQN()).thenReturn(new FullQualifiedName(namespace, externalName));
    return attribute;
  }

  private void setBindingParameter(final Method method) throws ODataJPAModelException, EdmPrimitiveTypeException {
    final JPAParameter bindingParam = addParameter(method, null, "Root", 0);

    doReturn(AdministrativeDivision.class).when(bindingParam).getType();
    final List<UriParameter> keys = new ArrayList<>();
    final UriParameter key1 = mock(UriParameter.class);
    when(bindingEntity.getKeyPredicates()).thenReturn(keys);
    when(key1.getName()).thenReturn("CodeID");
    when(key1.getText()).thenReturn("LAU2");
    keys.add(key1);

    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAPath codePath = mock(JPAPath.class);
    final JPAAttribute code = mock(JPAAttribute.class);
    final EdmPrimitiveType edmString = mock(EdmPrimitiveType.class);
    final CsdlProperty edmProperty = mock(CsdlProperty.class);
    when(sd.getEntity((EdmType) any())).thenReturn(et);
    when(et.getPath("CodeID")).thenReturn(codePath);
    when(et.getAttribute("codeID")).thenReturn(Optional.of(code));
    doReturn(AdministrativeDivision.class).when(et).getTypeClass();

    when(codePath.getLeaf()).thenReturn(code);
    when(code.getInternalName()).thenReturn("codeID");
    doReturn(String.class).when(code).getType();

    when(code.getProperty()).thenReturn(edmProperty);
    when(code.getEdmType()).thenReturn(EdmPrimitiveTypeKind.String);
    when(odata.createPrimitiveTypeInstance(EdmPrimitiveTypeKind.String)).thenReturn(edmString);
    when(edmString.fromUriLiteral("LAU2")).thenReturn("LAU2");
    when(edmString.valueOfString("LAU2", false, 0, 0, 0, true, code.getType())).thenAnswer(
        new Answer<String>() {
          @Override
          public String answer(final InvocationOnMock invocation) throws Throwable {
            return "LAU2";
          }
        });
  }

  private JPAParameter addParameter(final Method method, final Object value, final String name, final int index)
      throws ODataJPAModelException {

    final Parameter param = mock(Parameter.class);
    when(param.getValue()).thenReturn(value);
    when(param.getName()).thenReturn(name);
    when(param.getValueType()).thenReturn(ValueType.PRIMITIVE);
    actionParameter.put(name, param);

    final JPAParameter jpaParam = mock(JPAParameter.class);
    when(action.getParameter(method.getParameters()[index])).thenReturn(jpaParam);
    when(jpaParam.getName()).thenReturn(name);
    return jpaParam;
  }

  @SuppressWarnings("unchecked")
  private Method setConstructorAndMethod(final String methodName, final Class<?>... parameterTypes)
      throws NoSuchMethodException {
    @SuppressWarnings("rawtypes")
    final Constructor constructor = TestJavaActionNoParameter.class.getConstructors()[0];
    final Method method = TestJavaActionNoParameter.class.getMethod(methodName, parameterTypes);
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    when(action.getReturnType()).thenReturn(null);
    return method;
  }

  @SuppressWarnings("unchecked")
  private Method setConstructorAndMethod(final Class<?> clazz, final String methodName,
      final Class<?>... parameterTypes) throws NoSuchMethodException {
    @SuppressWarnings("rawtypes")
    final Constructor constructor = clazz.getConstructors()[0];
    final Method method = clazz.getMethod(methodName, parameterTypes);
    when(action.getConstructor()).thenReturn(constructor);
    when(action.getMethod()).thenReturn(method);
    when(action.getReturnType()).thenReturn(null);
    return method;
  }

}
