package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static com.sap.olingo.jpa.processor.core.processor.JPAETagValidationResult.NOT_MODIFIED;
import static com.sap.olingo.jpa.processor.core.processor.JPAETagValidationResult.PRECONDITION_FAILED;
import static com.sap.olingo.jpa.processor.core.processor.JPAETagValidationResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class JPANavigationRequestProcessorTest extends TestBase {

  private JPANavigationRequestProcessor cut;
  private JPAODataRequestContextAccess requestContext;
  private ServiceMetadata metadata;
  private JPAExpandResult result;
  private JPAHttpHeaderMap headers;
  private UriInfoResource uriInfo;
  private UriResource uriResource;
  private OData odata;

  @BeforeEach
  void setup() throws ODataException {
    getHelper();
    requestContext = mock(JPAODataRequestContextAccess.class);
    metadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfoResource.class);
    uriResource = mock(UriResource.class);
    result = mock(JPAExpandResult.class, withSettings().extraInterfaces(JPAConvertibleResult.class));
    headers = mock(JPAHttpHeaderMap.class);
    odata = OData.newInstance();

    when(requestContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(requestContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getEtagHelper()).thenReturn(new JPAODataEtagHelperImpl(odata));
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(uriResource));

    cut = new JPANavigationRequestProcessor(OData.newInstance(), metadata, requestContext);

    doReturn(helper.getJPAEntityType(Organization.class)).when(result).getEntityType();
  }

  @Test
  void testNoEtagPropertySuccess() throws ODataJPAProcessorException, ODataJPAModelException {
    final Tuple tuple = mock(Tuple.class);
    final List<Tuple> rootResult = Collections.singletonList(tuple);
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(rootResult);
    when(headers.get(HttpHeader.IF_MATCH)).thenReturn(Arrays.asList("\"1\""));

    doReturn(helper.getJPAEntityType(AdministrativeDivision.class)).when(result).getEntityType();
    assertEquals(JPAETagValidationResult.SUCCESS, cut.validateEntityTag((JPAConvertibleResult) result, headers));
  }

  @Test
  void testValidWithoutHeader() throws ODataJPAProcessorException {

    assertEquals(JPAETagValidationResult.SUCCESS, cut.validateEntityTag((JPAConvertibleResult) result, headers));
  }

  @Test
  void testValidIfMatchWithoutResult() throws ODataJPAProcessorException {
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(Collections.emptyList());
    when(headers.get(HttpHeader.IF_MATCH)).thenReturn(Arrays.asList("\"1\""));

    assertEquals(JPAETagValidationResult.SUCCESS, cut.validateEntityTag((JPAConvertibleResult) result, headers));
  }

  @Test
  void testValidIfNoneMatchWithoutResult() throws ODataJPAProcessorException {
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(Collections.emptyList());
    when(headers.get(HttpHeader.IF_NONE_MATCH)).thenReturn(Arrays.asList("\"0\""));

    assertEquals(JPAETagValidationResult.SUCCESS, cut.validateEntityTag((JPAConvertibleResult) result, headers));
  }

  private static Stream<Arguments> provideIfMatchHeader() {
    return Stream.of(
        Arguments.of(Arrays.asList("\"0\""), PRECONDITION_FAILED, "Not matching eTag: 412 ecxpected"),
        Arguments.of(Arrays.asList("\"0\"", "\"3\""), PRECONDITION_FAILED, "None matching eTag: 412 ecxpected"),
        Arguments.of(Arrays.asList("\"1\""), SUCCESS, "Matching eTag: 200 ecxpected"),
        Arguments.of(Arrays.asList("\"2\"", "\"1\""), SUCCESS, "One Matching eTag: 200 ecxpected"),
        Arguments.of(Arrays.asList("*"), SUCCESS, "* eTag: 200 ecxpected"));
  }

  @ParameterizedTest
  @MethodSource("provideIfMatchHeader")
  void testIfMatchHeader(final List<String> etag, final JPAETagValidationResult exp, final String message)
      throws ODataException {

    final Tuple tuple = mock(Tuple.class);
    final List<Tuple> rootResult = Collections.singletonList(tuple);
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(rootResult);
    when(headers.get(HttpHeader.IF_MATCH)).thenReturn(etag);

    when(tuple.get("ETag")).thenReturn(Integer.valueOf(1));

    assertEquals(exp, cut.validateEntityTag((JPAConvertibleResult) result, headers), message);

  }

  private static Stream<Arguments> provideIfNoneMatchHeader() {
    return Stream.of(
        Arguments.of(Arrays.asList("\"1\""), NOT_MODIFIED, "Matching eTag: 304 ecxpected"),
        Arguments.of(Arrays.asList("\"2\"", "\"1\""), NOT_MODIFIED, "One Matching eTag: 304 ecxpected"),
        Arguments.of(Arrays.asList("\"0\""), SUCCESS, "Not matching eTag: 200 ecxpected"),
        Arguments.of(Arrays.asList("\"0\"", "\"3\""), SUCCESS, "None matching eTag: 200 ecxpected"),
        Arguments.of(Arrays.asList("*"), NOT_MODIFIED, "* matches any eTag: 304 ecxpected"));
  }

  @ParameterizedTest
  @MethodSource("provideIfNoneMatchHeader")
  void testIfNoneMatchHeader(final List<String> etag, final JPAETagValidationResult exp, final String message)
      throws ODataException {

    final Tuple tuple = mock(Tuple.class);
    final List<Tuple> rootResult = Collections.singletonList(tuple);
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(rootResult);
    when(headers.get(HttpHeader.IF_NONE_MATCH)).thenReturn(etag);

    when(tuple.get("ETag")).thenReturn(Integer.valueOf(1));

    assertEquals(exp, cut.validateEntityTag((JPAConvertibleResult) result, headers), message);

  }

  private static Stream<Arguments> provideHeaderException() {
    return Stream.of(
        Arguments.of(Arrays.asList("\"1\""), HttpHeader.IF_MATCH),
        Arguments.of(Arrays.asList("\"0\""), HttpHeader.IF_NONE_MATCH));
  }

  @ParameterizedTest
  @MethodSource("provideHeaderException")
  void testThrowExceptionMultipleResults(final List<String> etag, final String header) {

    final Tuple tuple1 = mock(Tuple.class);
    final Tuple tuple2 = mock(Tuple.class);
    final List<Tuple> rootResult = Arrays.asList(tuple1, tuple2);
    when(result.getResult(ROOT_RESULT_KEY)).thenReturn(rootResult);
    when(headers.get(header)).thenReturn(etag);

    assertThrows(ODataJPAProcessorException.class,
        () -> cut.validateEntityTag((JPAConvertibleResult) result, headers));

  }
}
