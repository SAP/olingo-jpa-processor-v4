package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDate;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt16;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.serializer.JPAOperationSerializer;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionActionConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionReturnType;

class JPAFunctionRequestProcessorTest {

  private JPAFunctionRequestProcessor cut;
  private ContentType requestFormat;
  @Mock
  private ODataDeserializer deserializer;
  @Mock
  private JPAOperationSerializer serializer;
  @Mock
  private OData odata;
  @Mock
  private JPAODataRequestContextAccess requestContext;
  @Mock
  private ODataRequest request;
  @Mock
  private ODataResponse response;
  @Mock
  private JPAJavaFunction function;
  @Mock
  private JPAServiceDocument sd;
  @Mock
  private UriInfo uriInfo;
  private List<UriResource> uriResources;
  private List<UriParameter> uriParameters;
  @Mock
  private UriResourceFunction resource;
  @Mock
  private EdmFunction edmFunction;
  @Captor
  ArgumentCaptor<Annotatable> annotatableCaptor;
  @Mock
  private UriHelper uriHelper;
  @Mock
  private JPAODataEtagHelper etagHelper;

  @BeforeEach
  void setup() throws ODataException {
    MockitoAnnotations.openMocks(this);

    uriResources = new ArrayList<>();
    uriResources.add(resource);
    uriParameters = new ArrayList<>();

    final EntityManager em = mock(EntityManager.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    final JPAEdmProvider edmProvider = mock(JPAEdmProvider.class);
    final SerializerResult serializerResult = mock(SerializerResult.class);

    when(requestContext.getEntityManager()).thenReturn(em);
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);
    when(edmProvider.getServiceDocument()).thenReturn(sd);
    when(requestContext.getRequestParameter()).thenReturn(new JPARequestParameterHashMap());
    when(requestContext.getHeader()).thenReturn(new JPAHttpHeaderHashMap());
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(requestContext.getEtagHelper()).thenReturn(etagHelper);
    when(serializer.serialize(any(Annotatable.class), any(EdmType.class), any(ODataRequest.class)))
        .thenReturn(serializerResult);
    when(serializer.getContentType()).thenReturn(ContentType.APPLICATION_JSON);

    when(uriInfo.getUriResourceParts()).thenReturn(uriResources);
    when(resource.getFunction()).thenReturn(edmFunction);
    when(resource.getParameters()).thenReturn(uriParameters);
    when(edmFunction.isBound()).thenReturn(Boolean.FALSE);
    when(function.getFunctionType()).thenReturn(EdmFunctionType.JavaClass);
    when(sd.getFunction(edmFunction)).thenReturn(function);
    when(odata.createDeserializer((ContentType) any())).thenReturn(deserializer);
    when(odata.createUriHelper()).thenReturn(uriHelper);

    requestFormat = ContentType.APPLICATION_JSON;

    cut = new JPAFunctionRequestProcessor(odata, requestContext);
  }

  @Test
  void testCallsWithParameterValue() throws IllegalArgumentException, NoSuchMethodException,
      SecurityException, ODataApplicationException, ODataLibraryException, ODataJPAModelException {

    final Method method = setConstructorAndMethod("primitiveValue", short.class);

    final Triple<UriParameter, EdmParameter, JPAParameter> parameter =
        createParameter("A", "5", Short.class, method);

    final EdmReturnType returnType = mock(EdmReturnType.class);
    when(returnType.getType()).thenReturn(EdmInt32.getInstance());
    when(edmFunction.getReturnType()).thenReturn(returnType);
    when(edmFunction.getParameter("A")).thenReturn(parameter.getMiddle());

    final JPAOperationResultParameter resultParameter = mock(JPAOperationResultParameter.class);
    when(function.getResultParameter()).thenReturn(resultParameter);
    when(resultParameter.isCollection()).thenReturn(Boolean.FALSE);

    cut.retrieveData(request, response, requestFormat);
    verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
    verify(serializer).serialize(annotatableCaptor.capture(), eq(EdmInt32.getInstance()), eq(request));
    assertTrue(annotatableCaptor.getValue().toString().contains("5"));
  }

  @Test
  void testCallsWithParameterNull() throws NoSuchMethodException, ODataJPAModelException, ODataApplicationException,
      ODataLibraryException {

    final Method method = setConstructorAndMethod("primitiveValueNullable", Short.class);

    final Triple<UriParameter, EdmParameter, JPAParameter> parameter =
        createParameter("A", null, Short.class, method);

    final EdmReturnType returnType = mock(EdmReturnType.class);
    when(returnType.getType()).thenReturn(EdmInt32.getInstance());
    when(edmFunction.getReturnType()).thenReturn(returnType);
    when(edmFunction.getParameter("A")).thenReturn(parameter.getMiddle());

    final JPAOperationResultParameter resultParameter = mock(JPAOperationResultParameter.class);
    when(function.getResultParameter()).thenReturn(resultParameter);
    when(resultParameter.isCollection()).thenReturn(Boolean.FALSE);

    cut.retrieveData(request, response, requestFormat);
    verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
    verify(serializer).serialize(annotatableCaptor.capture(), eq(EdmInt32.getInstance()), eq(request));
    assertTrue(annotatableCaptor.getValue().toString().contains("0"));
  }

  @Test
  void testCallsWithConstructorParameterValue() throws NoSuchMethodException, ODataJPAModelException,
      ODataApplicationException, ODataLibraryException {

    final Method method = setConstructorAndMethod(TestFunctionActionConstructor.class, "func", LocalDate.class);

    final Triple<UriParameter, EdmParameter, JPAParameter> parameter =
        createParameter("date", "2024-10-03", LocalDate.class, method);

    final EdmReturnType returnType = mock(EdmReturnType.class);
    when(returnType.getType()).thenReturn(EdmBoolean.getInstance());
    when(edmFunction.getReturnType()).thenReturn(returnType);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    final JPAOperationResultParameter resultParameter = mock(JPAOperationResultParameter.class);
    when(function.getResultParameter()).thenReturn(resultParameter);
    when(resultParameter.isCollection()).thenReturn(Boolean.FALSE);

    cut.retrieveData(request, response, requestFormat);
    verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
    verify(serializer).serialize(annotatableCaptor.capture(), eq(EdmBoolean.getInstance()), eq(request));
    assertTrue(annotatableCaptor.getValue().toString().contains("true"));
  }

  @Test
  void testEtagHeaderFilledForEntity() throws NoSuchMethodException, ODataApplicationException, ODataLibraryException,
      ODataJPAModelException {
    setConstructorAndMethod(TestFunctionReturnType.class, "convertBirthday");

    final EdmReturnType returnType = mock(EdmReturnType.class);
    final EdmEntityType type = mock(EdmEntityType.class);
    when(returnType.getType()).thenReturn(type);
    when(edmFunction.getReturnType()).thenReturn(returnType);
    when(type.getKind()).thenReturn(EdmTypeKind.ENTITY);

    final JPAOperationResultParameter resultParameter = mock(JPAOperationResultParameter.class);
    when(function.getResultParameter()).thenReturn(resultParameter);
    when(resultParameter.isCollection()).thenReturn(Boolean.FALSE);

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
    when(etagHelper.asEtag(any(), any())).thenReturn("\"3\"");

    when(uriHelper.buildKeyPredicate(any(), any())).thenReturn("example.org");

    cut.retrieveData(request, response, requestFormat);
    verify(response, times(1)).setStatusCode(200);
    verify(response, times(1)).setHeader(HttpHeader.ETAG, "\"3\"");

  }

  private Method setConstructorAndMethod(final String methodName,
      final Class<?>... parameterTypes) throws NoSuchMethodException {
    return setConstructorAndMethod(TestFunctionReturnType.class, methodName, parameterTypes);
  }

  private JPAAttribute createJPAAttribute(final String internalName, final String namespace,
      final String externalName) {
    final JPAAttribute attribute = mock(JPAAttribute.class);
    when(attribute.getInternalName()).thenReturn(internalName);
    when(attribute.getExternalName()).thenReturn(externalName);
    when(attribute.getExternalFQN()).thenReturn(new FullQualifiedName(namespace, externalName));
    return attribute;
  }

  @SuppressWarnings("unchecked")
  private Method setConstructorAndMethod(final Class<?> clazz, final String methodName,
      final Class<?>... parameterTypes) throws NoSuchMethodException {
    @SuppressWarnings("rawtypes")
    final Constructor constructor = clazz.getConstructors()[0];
    final Method method = clazz.getMethod(methodName, parameterTypes);
    when(function.getConstructor()).thenReturn(constructor);
    when(function.getMethod()).thenReturn(method);
    when(function.getReturnType()).thenReturn(null);
    return method;
  }

  private Triple<UriParameter, EdmParameter, JPAParameter> createParameter(final String name, final String value,
      final Class<?> parameterType, final Method method) throws ODataJPAModelException {
    final UriParameter uriParameter = mock(UriParameter.class);
    when(uriParameter.getName()).thenReturn(name);
    when(uriParameter.getText()).thenReturn(value);
    uriParameters.add(uriParameter);

    final JPAParameter parameter = mock(JPAParameter.class);
    when(parameter.getName()).thenReturn(name);
    doReturn(parameterType).when(parameter).getType();
    when(function.getParameter(method.getParameters()[0])).thenReturn(parameter);

    final EdmParameter edmParameter = mock(EdmParameter.class);
    when(edmParameter.getType()).thenReturn(parameterType == LocalDate.class
        ? EdmDate.getInstance()
        : EdmInt16.getInstance());

    return new ImmutableTriple<>(uriParameter, edmParameter, parameter);
  }

}
