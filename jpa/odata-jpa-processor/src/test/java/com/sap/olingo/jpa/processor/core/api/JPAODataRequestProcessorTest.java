package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public class JPAODataRequestProcessorTest {

  private static JPAODataRequestProcessor cut;
  private static EntityManager em;
  private static JPAODataClaimsProvider claims;
  private static JPAODataSessionContextAccess context;
  private static ODataRequest request;
  private static ODataResponse response;
  private static UriInfo uriInfo;
  private static OData odata;
  private static ServiceMetadata serviceMetadata;
  private static List<UriResource> resourceParts;

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

  static Stream<Executable> modifyComplexValueMethodsProvider() {
    return Stream.of(() -> {
      cut.updateComplex(null, null, null, null, null);
    }, () -> {
      cut.deleteComplex(null, null, null);
    });
  }

  static Stream<Executable> throwsSerializerExceptionMethodsProvider() throws SerializerException {
    when(odata.createSerializer(ContentType.APPLICATION_JSON)).thenThrow(SerializerException.class);
    return Stream.of(() -> {
      cut.createEntity(request, response, uriInfo, ContentType.APPLICATION_JSON, ContentType.APPLICATION_JSON);
    }, () -> {
      cut.updateEntity(request, response, uriInfo, ContentType.APPLICATION_JSON, ContentType.APPLICATION_JSON);
//    }, () -> {
//      cut.deleteEntity(request, response, uriInfo);
    }, () -> {
      cut.readEntity(request, response, uriInfo, ContentType.APPLICATION_JSON);
    });
  }

  @BeforeAll
  public static void classSetup() {
    em = mock(EntityManager.class);
    claims = new JPAODataClaimsProvider();
    context = mock(JPAODataSessionContextAccess.class);
    request = mock(ODataRequest.class);
    response = mock(ODataResponse.class);
    uriInfo = mock(UriInfo.class);
    odata = mock(OData.class);
    serviceMetadata = mock(ServiceMetadata.class);
    resourceParts = new ArrayList<>(1);
    UriResource resourcePart = mock(UriResource.class);
    resourceParts.add(resourcePart);

    when(uriInfo.getUriResourceParts()).thenReturn(resourceParts);
    when(resourcePart.getKind()).thenReturn(UriResourceKind.navigationProperty);

    cut = new JPAODataRequestProcessor(context, claims, em);
    cut.init(odata, serviceMetadata);
  }

  @ParameterizedTest
  @MethodSource("modifyMediaTypeMethodsProvider")
  public void checkModifyMediaEntityThrowsNotImplemented(final Executable m) {

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, m);
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("updatePrimitiveValueMethodsProvider")
  public void checkUpdatePrimitveValueThrowsNotImplemented(final Executable m) {

    final ODataJPAProcessorException act = assertThrows(ODataJPAProcessorException.class, m);
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("throwsSerializerExceptionMethodsProvider")
  public void checkCreateEntityPropagateSerializerException(final Executable m) throws SerializerException {

    // when(odata.createSerializer(ContentType.APPLICATION_JSON)).thenThrow(SerializerException.class);
    assertThrows(ODataException.class, m);
  }

  @Test
  public void checkUpdateEntityPropagateSerializerException() throws SerializerException {

    // when(odata.createSerializer(ContentType.APPLICATION_JSON)).thenThrow(SerializerException.class);
    assertThrows(ODataException.class, () -> {
      cut.updateEntity(request, response, uriInfo, ContentType.APPLICATION_JSON, ContentType.APPLICATION_JSON);
    });
  }
}
