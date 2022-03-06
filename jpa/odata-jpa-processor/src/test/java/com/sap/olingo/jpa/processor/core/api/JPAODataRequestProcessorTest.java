package com.sap.olingo.jpa.processor.core.api;

import static com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException.MessageKeys.MODIFY_NOT_ALLOWED;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.NO_METADATA_PROVIDER;
import static org.apache.olingo.commons.api.format.ContentType.JSON;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.prefer.Preferences;
import org.apache.olingo.server.api.prefer.Preferences.Return;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.example.JPAExampleModifyException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.testobjects.TestJavaActionNoParameter;

class JPAODataRequestProcessorTest {

  private static final String ODATA_VERSION = "4.00";
  private static JPAODataRequestProcessor cut;
  private static EntityManager em;
  private static JPAODataClaimsProvider claims;
  private JPAODataSessionContextAccess sessionContext;
  private static ODataRequest request;
  private static ODataResponse response;
  private static UriInfo uriInfo;
  private OData odata;
  private static ServiceMetadata serviceMetadata;
  private static List<UriResource> resourceParts;
  private JPAODataRequestContextAccess requestContext;
  private ODataDeserializer deserializer;
  private ODataSerializer serializer;
  private SerializerResult serializerResult;
  private static JPAServiceDebugger debugger;
  private static JPAEntityType et;

  static Stream<Executable> modifyMediaTypeMethodsProvider() {
    return Stream.of(() -> {
      cut.createMediaEntity(null, null, null, null, null);
    }, () -> {
      cut.updateMediaEntity(null, null, null, null, null);
    }, () -> {
      cut.deleteMediaEntity(null, null, null);
    });
  }

  static Stream<Executable> updatePrimitiveValueMethodsProvider() {
    return Stream.of(() -> {
      cut.updatePrimitiveValue(null, null, null, null, null);
    });
  }

  static Stream<Executable> notSupportedMethodsProvider() {
    return Stream.of(
        () -> {
          cut.updatePrimitiveValue(null, null, null, null, null);
        },
        () -> {
          cut.updateComplex(null, null, null, null, null);
        },
        () -> {
          cut.countComplexCollection(request, response, uriInfo);
        },
        () -> {
          cut.deletePrimitiveValue(request, response, uriInfo);
        });
  }

  static Stream<Executable> supportedDeletingMethodsProvider() throws SerializerException {
    return Stream.of(
        () -> {
          cut.deleteEntity(request, response, uriInfo);
        },
        () -> {
          cut.deleteComplex(request, response, uriInfo);
        },
        () -> {
          cut.deletePrimitive(request, response, uriInfo);
        },
        () -> {
          cut.deleteComplexCollection(request, response, uriInfo);
        },
        () -> {
          cut.deletePrimitiveCollection(request, response, uriInfo);
        });
  }

  static Stream<Executable> supportedUpdatingMethodsProvider() throws SerializerException {

    return Stream.of(
        () -> {
          cut.updateComplexCollection(request, response, uriInfo, JSON, JSON);
        },
        () -> {
          cut.updatePrimitive(request, response, uriInfo, JSON,
              JSON);
        },
        () -> {
          cut.updatePrimitiveCollection(request, response, uriInfo, JSON,
              JSON);
        });
  }

  static Stream<Executable> supportedCreatingMethodsProvider() throws SerializerException {

    return Stream.of(
        () -> {
          cut.createEntity(request, response, uriInfo, JSON, JSON);
        });
  }

  static Stream<Executable> supportedModifyingMethodsProvider() throws SerializerException {

    return Stream.concat(
        Stream.concat(supportedUpdatingMethodsProvider(), supportedDeletingMethodsProvider()),
        supportedCreatingMethodsProvider());
  }

  static Stream<Executable> supportedReadingMethodsProvider() throws SerializerException {

    return Stream.of(
        () -> {
          cut.countEntityCollection(request, response, uriInfo);
        },
        () -> {
          cut.createEntity(request, response, uriInfo, JSON, JSON);
        },
        () -> {
          cut.readComplex(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readComplexCollection(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readEntity(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readEntityCollection(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readPrimitive(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readPrimitiveCollection(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readPrimitiveValue(request, response, uriInfo, JSON);
        },
        () -> {
          cut.readMediaEntity(request, response, uriInfo, JSON);
        },
        () -> {
          cut.processActionPrimitive(request, response, uriInfo, JSON,
              JSON);
        },
        () -> {
          cut.processActionVoid(request, response, uriInfo, JSON);
        });
  }

  static Stream<Executable> supportedMethodsProvider() throws SerializerException {

    return Stream.concat(supportedModifyingMethodsProvider(), supportedReadingMethodsProvider());
  }

  static Stream<Executable> throwsSerializerExceptionMethodsProvider() throws SerializerException {
    // when(odata.createSerializer(JSON)).thenThrow(SerializerException.class);
    return Stream.of(() -> {
      cut.createEntity(request, response, uriInfo, JSON, JSON);
    }, () -> {
      cut.updateEntity(request, response, uriInfo, JSON, JSON);
    }, () -> {
      cut.readEntity(request, response, uriInfo, JSON);
    });
  }

  @BeforeAll
  public static void classSetup() {
    em = mock(EntityManager.class);
    claims = new JPAODataClaimsProvider();

    request = mock(ODataRequest.class);
    response = mock(ODataResponse.class);
    uriInfo = mock(UriInfo.class);
    serviceMetadata = mock(ServiceMetadata.class);
    resourceParts = new ArrayList<>(0);
    final UriResource resourcePart = mock(UriResource.class);
    resourceParts.add(resourcePart);
    debugger = mock(JPAServiceDebugger.class);
    et = mock(JPAEntityType.class);
    when(uriInfo.getUriResourceParts()).thenReturn(resourceParts);
    when(resourcePart.getKind()).thenReturn(UriResourceKind.navigationProperty);
  }

  @BeforeEach
  void setup() throws ODataException {
    final List<String> versionList = Collections.singletonList(ODATA_VERSION);
    final Preferences prefer = mock(Preferences.class);
    odata = mock(OData.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    deserializer = mock(ODataDeserializer.class);
    serializer = mock(ODataSerializer.class);
    serializerResult = mock(SerializerResult.class);
    createServiceDocument();
    when(requestContext.getClaimsProvider()).thenReturn(Optional.ofNullable(claims));
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    when(requestContext.getHeader()).thenReturn(mock(JPAHttpHeaderMap.class));
    when(requestContext.getDebugger()).thenReturn(debugger);
    when(odata.createDeserializer(any())).thenReturn(deserializer);
    when(odata.createDeserializer(any(), eq(versionList))).thenReturn(deserializer);
    when(odata.createPreferences(any())).thenReturn(prefer);
    when(odata.createSerializer(any(), anyList())).thenReturn(serializer);
    when(prefer.getReturn()).thenReturn(Return.MINIMAL);
    when(request.getHeaders(HttpHeader.ODATA_VERSION)).thenReturn(versionList);

    final DeserializerResult deserializerResult = mock(DeserializerResult.class);
    final Entity entity = mock(Entity.class);
    when(deserializer.entity(any(), any())).thenReturn(deserializerResult);
    when(deserializerResult.getEntity()).thenReturn(entity);

    when(serializer.primitive(any(), any(), any(), any())).thenReturn(serializerResult);

    cut = new JPAODataRequestProcessor(sessionContext, requestContext);
    cut.init(odata, serviceMetadata);
  }

  @ParameterizedTest
  @MethodSource("modifyMediaTypeMethodsProvider")
  void checkModifyMediaEntityThrowsNotImplemented(final Executable m) {

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, m);
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("notSupportedMethodsProvider")
  void checkNutSupportedThrowsNotImplemented(final Executable m) {

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, m);
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("throwsSerializerExceptionMethodsProvider")
  void checkCreateEntityPropagateSerializerException(final Executable m) throws SerializerException {
    when(odata.createSerializer(JSON, Collections.emptyList()))
        .thenThrow(SerializerException.class);

    assertThrows(ODataException.class, m);
  }

  @Test
  void checkUpdateEntityPropagateSerializerException() throws SerializerException {
    when(odata.createSerializer(JSON, Collections.emptyList()))
        .thenThrow(SerializerException.class);

    assertThrows(ODataException.class, () -> {
      cut.updateEntity(request, response, uriInfo, JSON, JSON);
    });
  }

  @Test
  void checkDeleteEntityCallsDelete() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);

    resourceParts.add(entitySet);

    cut.deleteEntity(request, response, uriInfo);

    verify(handler).deleteEntity(any(), eq(em));
  }

  @Test
  void checkDeleteEntityRethrowExceptionOnHandlerFail() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);

    resourceParts.add(entitySet);
    doThrow(new JPAExampleModifyException(
        MODIFY_NOT_ALLOWED, HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(), eq(em));

    assertThrows(JPAExampleModifyException.class, () -> cut.deleteEntity(request, response, uriInfo));
  }

  static Stream<Pair<Executable, UriResourceProperty>> deletingMethods() throws SerializerException,
      ODataJPAModelException {

    final Executable deleteComplex = () -> {
      cut.deleteComplex(request, response, uriInfo);
    };
    final Executable deleteComplexCollection = () -> {
      cut.deleteComplexCollection(request, response, uriInfo);
    };
    final Executable deletePrimitive = () -> {
      cut.deletePrimitive(request, response, uriInfo);
    };
    final Executable deletePrimitiveCollection = () -> {
      cut.deletePrimitiveCollection(request, response, uriInfo);
    };

    return Stream.of(
        new ImmutablePair<>(deleteComplex, createComplexType(et)),
        new ImmutablePair<>(deleteComplexCollection, createComplexType(et)),
        new ImmutablePair<>(deletePrimitive, createPrimitiveType(et)),
        new ImmutablePair<>(deletePrimitiveCollection, createPrimitiveType(et)));

  }

  @ParameterizedTest
  @MethodSource("deletingMethods")
  void checkDeleteElementCallsUpdate(final Pair<Executable, UriResourceProperty> test) throws Throwable {
    when(request.getMethod()).thenReturn(HttpMethod.DELETE);
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);

    resourceParts.add(entitySet);
    resourceParts.add(test.getRight());
    test.getLeft().execute();
    verify(handler).updateEntity(any(), eq(em), eq(HttpMethod.DELETE));
  }

  @ParameterizedTest
  @MethodSource("deletingMethods")
  void checkDeleteElementRethrowsRollbackAsPreconditionFailed(final Pair<Executable, UriResourceProperty> test)
      throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);

    when(handler.updateEntity(any(), any(), any())).thenThrow(new RollbackException(
        new OptimisticLockException()));

    resourceParts.add(entitySet);
    resourceParts.add(test.getRight());

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, test.getLeft());

    assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), act.getStatusCode());
  }

  @Test
  void checkDeleteEntityRethrowsRollbackAsPreconditionFailed() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);

    doThrow(new RollbackException(new OptimisticLockException())).when(handler).deleteEntity(any(), eq(em));

    resourceParts.add(entitySet);

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.deleteEntity(
        request, response, uriInfo));

    assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), act.getStatusCode());
  }

  @Test
  void checkDeletePrimitiveRethrowExceptionOnHandlerFail() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    final UriResourcePrimitiveProperty primitiveProperty = createPrimitiveType(et);

    resourceParts.add(entitySet);
    resourceParts.add(primitiveProperty);

    when(handler.updateEntity(any(), eq(em), eq(HttpMethod.DELETE))).thenThrow(new JPAExampleModifyException(
        MODIFY_NOT_ALLOWED, HttpStatusCode.BAD_REQUEST));

    assertThrows(JPAExampleModifyException.class, () -> cut.deletePrimitive(request, response, uriInfo));
  }

  @Test
  void checkDeletePrimitiveCollectionRethrowExceptionOnHandlerFail() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    final UriResourcePrimitiveProperty primitiveProperty = createPrimitiveType(et);

    resourceParts.add(entitySet);
    resourceParts.add(primitiveProperty);

    when(handler.updateEntity(any(), eq(em), eq(HttpMethod.DELETE))).thenThrow(new JPAExampleModifyException(
        MODIFY_NOT_ALLOWED, HttpStatusCode.BAD_REQUEST));

    assertThrows(JPAExampleModifyException.class, () -> cut.deletePrimitiveCollection(request, response, uriInfo));
  }

  @Test
  void checkDeleteComplexCollectionRethrowExceptionOnHandlerFail() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.DELETE);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    final UriResourceComplexProperty complexProperty = createComplexType(et);

    resourceParts.add(entitySet);
    resourceParts.add(complexProperty);

    when(handler.updateEntity(any(), eq(em), eq(HttpMethod.DELETE))).thenThrow(new JPAExampleModifyException(
        MODIFY_NOT_ALLOWED, HttpStatusCode.BAD_REQUEST));

    assertThrows(JPAExampleModifyException.class, () -> cut.deleteComplexCollection(request, response, uriInfo));
  }

  @ParameterizedTest
  @MethodSource("supportedMethodsProvider")
  void checkThrowProcessorExceptionOnODataException(final Executable m) throws ODataException {
    final ODataSerializer serializer = mock(ODataSerializer.class);
    when(odata.createSerializer(JSON, Collections.emptyList())).thenReturn(serializer);
    prepareRequestThrowsException();
    final ODataApplicationException act = assertThrows(ODataApplicationException.class, m);
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void checkUpdateEntityCallsUpdate() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.PATCH);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    final JPAUpdateResult result = new JPAUpdateResult(false, null);
    when(handler.updateEntity(any(), any(), any())).thenReturn(result);

    resourceParts.add(entitySet);

    cut.updateEntity(request, response, uriInfo, ContentType.JSON, ContentType.JSON);

    verify(handler).updateEntity(any(), eq(em), eq(HttpMethod.PATCH));
  }

  @Test
  void checkUpdateEntityRethrowsRollbackAsPreconditionFailed() throws ODataException {

    when(request.getMethod()).thenReturn(HttpMethod.PATCH);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    when(handler.updateEntity(any(), any(), any())).thenThrow(new RollbackException(
        new OptimisticLockException()));

    resourceParts.add(entitySet);

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, () -> cut.updateEntity(
        request, response, uriInfo, ContentType.JSON, ContentType.JSON));

    assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), act.getStatusCode());
  }

  @Test
  void checkActionWithReturnIsPerformed() throws ODataException, NoSuchMethodException, SecurityException {

    when(request.getMethod()).thenReturn(HttpMethod.POST);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final EdmPrimitiveType edmReturnType = mock(EdmPrimitiveType.class);
    final EdmReturnType returnType = mock(EdmReturnType.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final UriResourceAction action = createAction(sd, et, returnType);
    when(returnType.isCollection()).thenReturn(Boolean.FALSE);
    when(returnType.getType()).thenReturn(edmReturnType);
    when(edmReturnType.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);

    resourceParts.add(action);

    cut.processActionPrimitive(request, response, uriInfo, JSON, JSON);

    verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
  }

  @Test
  void checkActionWithVoidIsPerformed() throws ODataException, NoSuchMethodException, SecurityException {
    final ODataResponse response = mock(ODataResponse.class);
    when(request.getMethod()).thenReturn(HttpMethod.POST);

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final UriResourceAction action = createAction(sd, et, null);

    resourceParts.add(action);

    cut.processActionVoid(request, response, uriInfo, JSON);

    verify(response).setStatusCode(HttpStatusCode.OK.getStatusCode());
  }

  private UriResourceAction createAction(final JPAServiceDocument sd, final JPAEntityType et,
      final EdmReturnType returnType) throws DeserializerException, NoSuchMethodException, SecurityException {

    final JPAAction jpaAction = mock(JPAAction.class);
    final EdmAction edmAction = mock(EdmAction.class);
    final UriResourceAction action = mock(UriResourceAction.class);
    when(action.getKind()).thenReturn(UriResourceKind.action);
    when(action.getAction()).thenReturn(edmAction);
    when(edmAction.getReturnType()).thenReturn(returnType);

    when(sd.getAction(edmAction)).thenReturn(jpaAction);
    when(jpaAction.getConstructor()).thenAnswer(new Answer<Constructor<?>>() {
      @Override
      public Constructor<?> answer(final InvocationOnMock invocation) throws Throwable {
        return TestJavaActionNoParameter.class.getConstructor();
      }
    });
    final Method m = TestJavaActionNoParameter.class.getMethod("unboundReturnPrimitiveNoParameter");
    when(jpaAction.getMethod()).thenReturn(m);
    final DeserializerResult deserializerValue = mock(DeserializerResult.class);
    when(deserializer.actionParameters(any(), any())).thenReturn(deserializerValue);
    when(deserializerValue.getActionParameters()).thenReturn(Collections.emptyMap());

    final JPAOperationResultParameter jpaResultParameter = mock(JPAOperationResultParameter.class);
    when(jpaAction.getResultParameter()).thenReturn(jpaResultParameter);
    when(jpaResultParameter.isCollection()).thenReturn(Boolean.FALSE);
    return action;
  }

  private UriResourceEntitySet createEntitySet(final JPAServiceDocument sd, final JPAEntityType et)
      throws ODataJPAModelException {
    final EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    final UriResourceEntitySet entitySet = mock(UriResourceEntitySet.class);
    final List<UriParameter> keyPredicate = new ArrayList<>();
    when(entitySet.getKind()).thenReturn(UriResourceKind.entitySet);
    when(entitySet.getEntitySet()).thenReturn(edmEntitySet);
    when(entitySet.getKeyPredicates()).thenReturn(keyPredicate);
    when(edmEntitySet.getName()).thenReturn("Organizations");
    when(sd.getEntity(anyString())).thenReturn(et);
    return entitySet;
  }

  private JPAServiceDocument prepareRequest(final JPACUDRequestHandler handler) throws ODataException {
    resourceParts.clear();
    final JPAServiceDocument sd = createServiceDocument();
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);
    return sd;
  }

  private JPAServiceDocument createServiceDocument() throws ODataException {
    final JPAServiceDocument sd = mock(JPAServiceDocument.class);
    final JPAEdmProvider edmProvider = mock(JPAEdmProvider.class);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);
    when(edmProvider.getServiceDocument()).thenReturn(sd);
    return sd;
  }

  private void prepareRequestThrowsException() throws ODataException {
    resourceParts.clear();
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    final JPAServiceDocument sd = prepareRequest(handler);
    final JPAEntityType et = mock(JPAEntityType.class);
    final UriResourceEntitySet entitySet = createEntitySet(sd, et);
    resourceParts.add(entitySet);
    when(requestContext.getEdmProvider())
        .thenThrow(new ODataJPAProcessorException(NO_METADATA_PROVIDER, INTERNAL_SERVER_ERROR));
  }

  private static UriResourceComplexProperty createComplexType(final JPAEntityType et) throws ODataJPAModelException {
    final EdmProperty edmProperty = mock(EdmProperty.class);
    final UriResourceComplexProperty complexProperty = mock(UriResourceComplexProperty.class);
    final JPAPath path = mock(JPAPath.class);
    final JPAAttribute jpaAttribute = mock(JPAAttribute.class);
    when(complexProperty.getKind()).thenReturn(UriResourceKind.complexProperty);
    when(complexProperty.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("Address");
    when(et.getPath("Address")).thenReturn(path);
    when(path.getLeaf()).thenReturn(jpaAttribute);
    when(jpaAttribute.getInternalName()).thenReturn("address");
    return complexProperty;
  }

  private static UriResourcePrimitiveProperty createPrimitiveType(final JPAEntityType et)
      throws ODataJPAModelException {
    final EdmProperty edmProperty = mock(EdmProperty.class);
    final UriResourcePrimitiveProperty primitiveProperty = mock(UriResourcePrimitiveProperty.class);
    final JPAPath path = mock(JPAPath.class);
    final JPAAttribute jpaAttribute = mock(JPAAttribute.class);
    when(primitiveProperty.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(primitiveProperty.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("Name1");
    when(et.getPath("Name1")).thenReturn(path);
    when(path.getLeaf()).thenReturn(jpaAttribute);
    when(jpaAttribute.getInternalName()).thenReturn("name1");
    return primitiveProperty;
  }

}
