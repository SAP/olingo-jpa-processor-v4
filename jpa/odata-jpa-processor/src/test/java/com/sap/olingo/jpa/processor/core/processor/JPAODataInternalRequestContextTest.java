package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.debug.DebugSupport;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataApiVersionAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPathInformation;
import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives;
import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives.UuidSortOrder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUserQueryExtension;

class JPAODataInternalRequestContextTest {
  private JPAODataInternalRequestContext cut;
  private JPAODataRequestContextAccess contextAccess;
  private JPAODataRequestContext requestContext;
  private JPAODataSessionContextAccess sessionContext;
  private Map<String, List<String>> header;
  private JPASerializer serializer;
  private JPAODataTransactionFactory transactionFactory;
  private JPARequestParameterMap customParameter;
  private UriInfo uriInfo;
  private EntityManager em;
  private Optional<JPAODataClaimProvider> claims;
  private Optional<JPAODataGroupProvider> groups;
  private JPAServiceDebugger debugger;
  private List<Locale> locales;
  private UriInfoResource uriInfoResource;
  private JPACUDRequestHandler cudHandler;
  private DebugSupport debugSupport;
  private JPAODataDatabaseProcessor dbProcessor;
  private JPAEdmProvider edmProvider;
  private JPAODataDatabaseOperations operationConverter;
  private JPAODataQueryDirectives queryDirectives;
  private JPAODataEtagHelper etagHelper;
  private JPAODataPagingProvider pagingProvider;
  private OData odata;
  private ETagHelper olingoEtagHelper;
  private JPAODataPathInformation pathInformation;
  private JPAODataApiVersionAccess version;

  @BeforeEach
  void setup() {
    contextAccess = mock(JPAODataRequestContextAccess.class);
    requestContext = mock(JPAODataRequestContext.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    version = mock(JPAODataApiVersionAccess.class);
    odata = mock(OData.class);
    olingoEtagHelper = mock(ETagHelper.class);
    uriInfoResource = mock(UriInfoResource.class);
    header = new HashMap<>();

    customParameter = mock(JPARequestParameterMap.class);
    transactionFactory = mock(JPAODataTransactionFactory.class);
    uriInfo = mock(UriInfo.class);
    em = mock(EntityManager.class);
    claims = Optional.empty();
    groups = Optional.empty();
    debugger = mock(JPAServiceDebugger.class);
    locales = new LinkedList<>();
    cudHandler = mock(JPACUDRequestHandler.class);
    debugSupport = mock(DebugSupport.class);
    dbProcessor = mock(JPAODataDatabaseProcessor.class);
    edmProvider = mock(JPAEdmProvider.class);
    operationConverter = mock(JPAODataDatabaseOperations.class);
    queryDirectives = new JPAODataQueryDirectives.JPAODataQueryDirectivesImpl(0, UuidSortOrder.AS_STRING);
    pagingProvider = mock(JPAODataPagingProvider.class);
    etagHelper = mock(JPAODataEtagHelper.class);
    pathInformation = new JPAODataPathInformation("", "", "", "");

    when(odata.createETagHelper()).thenReturn(olingoEtagHelper);

    when(contextAccess.getTransactionFactory()).thenReturn(transactionFactory);
    when(contextAccess.getRequestParameter()).thenReturn(customParameter);
    when(contextAccess.getEntityManager()).thenReturn(em);
    when(contextAccess.getClaimsProvider()).thenReturn(claims);
    when(contextAccess.getGroupsProvider()).thenReturn(groups);
    when(contextAccess.getProvidedLocale()).thenReturn(locales);
    when(contextAccess.getDebugger()).thenReturn(debugger);
    when(contextAccess.getQueryDirectives()).thenReturn(queryDirectives);
    when(contextAccess.getEtagHelper()).thenReturn(etagHelper);
    when(contextAccess.getPagingProvider()).thenReturn(Optional.of(pagingProvider));
    when(contextAccess.getPathInformation()).thenReturn(pathInformation);

    when(requestContext.getClaimsProvider()).thenReturn(claims);
    when(requestContext.getCUDRequestHandler()).thenReturn(cudHandler);
    when(requestContext.getGroupsProvider()).thenReturn(groups);
    when(requestContext.getTransactionFactory()).thenReturn(transactionFactory);
    when(requestContext.getRequestParameter()).thenReturn(customParameter);
    when(requestContext.getLocales()).thenReturn(locales);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getDebuggerSupport()).thenReturn(debugSupport);
    when(requestContext.getVersion()).thenReturn(JPAODataApiVersionAccess.DEFAULT_VERSION);
    when(debugSupport.isUserAuthorized()).thenReturn(Boolean.TRUE);

    when(version.getId()).thenReturn(JPAODataApiVersionAccess.DEFAULT_VERSION);
    when(version.getEdmProvider()).thenReturn(edmProvider);

    when(sessionContext.getDatabaseProcessor()).thenReturn(dbProcessor);
    when(sessionContext.getOperationConverter()).thenReturn(operationConverter);
    when(sessionContext.getQueryDirectives()).thenReturn(queryDirectives);
    when(sessionContext.getPagingProvider()).thenReturn(pagingProvider);
    when(sessionContext.getApiVersion(anyString())).thenReturn(version);
  }

  @Test
  void testCreateFromContextUriAccessWithDebugger() throws ODataJPAProcessorException {

    cut = new JPAODataInternalRequestContext(uriInfoResource, serializer, contextAccess, header, pathInformation);

    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(serializer, cut.getSerializer());
    assertEquals(header, cut.getHeader());
    assertNotEquals(customParameter, cut.getRequestParameter());
    assertEquals(uriInfoResource, cut.getUriInfo());
    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
    assertEquals(locales, cut.getProvidedLocale());
    assertNotNull(cut.getCUDRequestHandler());
    assertEquals(queryDirectives, cut.getQueryDirectives());
    assertEquals(etagHelper, cut.getEtagHelper());
    assertEquals(pagingProvider, cut.getPagingProvider().get());
    assertEquals(pathInformation, cut.getPathInformation());
  }

  @Test
  void testCreateFromContextUriContextAccess() throws ODataJPAProcessorException {

    when(contextAccess.getHeader()).thenReturn(new JPAHttpHeaderHashMap(header));
    cut = new JPAODataInternalRequestContext(uriInfoResource, contextAccess);

    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(serializer, cut.getSerializer());
    assertEquals(header, cut.getHeader());
    assertNotEquals(customParameter, cut.getRequestParameter());
    assertEquals(uriInfoResource, cut.getUriInfo());
    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
    assertEquals(locales, cut.getProvidedLocale());
    assertNotNull(cut.getCUDRequestHandler());
    assertEquals(queryDirectives, cut.getQueryDirectives());
    assertEquals(etagHelper, cut.getEtagHelper());
    assertEquals(pagingProvider, cut.getPagingProvider().get());
    assertEquals(pathInformation, cut.getPathInformation());
  }

  @Test
  void testCreateFromContextUriContextAccessHeader() throws ODataJPAProcessorException {

    cut = new JPAODataInternalRequestContext(uriInfoResource, contextAccess, header);

    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(serializer, cut.getSerializer());
    assertEquals(header, cut.getHeader());
    assertNotEquals(customParameter, cut.getRequestParameter());
    assertEquals(uriInfoResource, cut.getUriInfo());
    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
    assertEquals(locales, cut.getProvidedLocale());
    assertNotNull(cut.getCUDRequestHandler());
    assertEquals(queryDirectives, cut.getQueryDirectives());
    assertEquals(etagHelper, cut.getEtagHelper());
    assertEquals(pagingProvider, cut.getPagingProvider().get());
  }

  @Test
  void testCreateFromRequestContextWithDebugSupport() {
    // contextAccess.get
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);

    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertEquals(cudHandler, cut.getCUDRequestHandler());
    assertTrue(cut.getDebugSupport().isUserAuthorized());
    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(locales, cut.getProvidedLocale());
    assertEquals(customParameter, cut.getRequestParameter());
    assertEquals(queryDirectives, cut.getQueryDirectives());
    assertEquals(pagingProvider, cut.getPagingProvider().get());
  }

  @Test
  void testCreateFromContextAccessWithDebugSupport() throws ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, cut, header, pathInformation);
    assertTrue(cut.getDebugSupport().isUserAuthorized());
  }

  @Test
  void testGetLocaleLocaleNotEmpty() throws ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);
    locales.add(Locale.JAPAN);
    locales.add(Locale.CANADA_FRENCH);

    assertEquals(Locale.JAPAN, cut.getLocale());
  }

  @Test
  void testGetLocaleLocaleEmpty() throws ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    assertEquals(Locale.ENGLISH, cut.getLocale());
  }

  @Test
  void testGetLocaleLocaleNull() throws ODataJPAProcessorException {
    when(contextAccess.getProvidedLocale()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    assertEquals(Locale.ENGLISH, cut.getLocale());
  }

  @Test
  void testGetTransactionFactoryIfNull() throws ODataJPAProcessorException {
    when(contextAccess.getTransactionFactory()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    assertTrue(cut.getTransactionFactory() instanceof JPAODataDefaultTransactionFactory);
  }

  @Test
  void testGetDebuggerReturnsDefaultIfNullAndFalse() throws ODataJPAProcessorException {
    when(contextAccess.getDebugger()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void testGetQueryEnhancement() throws ODataJPAModelException, ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAQueryExtension extension = mock(JPAQueryExtension.class);
    when(et.getQueryExtension()).thenReturn(Optional.of(extension));
    when(extension.getConstructor()).thenAnswer(new Answer<Constructor<? extends EdmQueryExtensionProvider>>() {
      @Override
      public Constructor<? extends EdmQueryExtensionProvider> answer(final InvocationOnMock invocation)
          throws Throwable {
        return (Constructor<? extends EdmQueryExtensionProvider>) CurrentUserQueryExtension.class
            .getConstructors()[0];
      }
    });
    assertTrue(cut.getQueryEnhancement(et).isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculator() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> constructor = DummyPropertyCalculator.class.getConstructor(EntityManager.class);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) constructor);
    assertTrue(cut.getCalculator(attribute).isPresent());

  }

  @Test
  void testSetUriInfoThrowsExceptionPageExists() throws ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);

    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setUriInfo(uriInfo));
  }

  @Test
  void testSetSerializer() throws ODataJPAProcessorException {
    final JPASerializer jpaSerializer = mock(JPASerializer.class);
    cut = new JPAODataInternalRequestContext(uriInfo, serializer, contextAccess, header, pathInformation);
    cut.setJPASerializer(jpaSerializer);

    assertEquals(jpaSerializer, cut.getSerializer());
  }

  @Test
  void testGetDatabaseProcessor() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertEquals(dbProcessor, cut.getDatabaseProcessor());
  }

  @Test
  void testGetEdmProvider() throws ODataException {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertEquals(edmProvider, cut.getEdmProvider());
  }

  @Test
  void testGetEdmProviderRestricted() throws ODataException {
    final var groupsProvider = mock(JPAODataGroupProvider.class);
    final List<String> groupList = List.of("Company");
    final var restrictedProvider = mock(JPAEdmProvider.class);

    when(requestContext.getGroupsProvider()).thenReturn(Optional.of(groupsProvider));
    when(groupsProvider.getGroups()).thenReturn(groupList);
    when(edmProvider.asUserGroupRestricted(groupList)).thenReturn(restrictedProvider);

    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertEquals(restrictedProvider, cut.getEdmProvider());
  }

  @Test
  void testGetOperationConverter() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertEquals(operationConverter, cut.getOperationConverter());
  }

  @Test
  void testGetEtagHelper() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertNotNull(cut.getEtagHelper());
  }

  @Test
  void testTakesVersion() throws ODataJPAProcessorException {
    final var emf = mock(EntityManagerFactory.class);
    when(emf.createEntityManager()).thenReturn(em);

    when(requestContext.getEntityManager()).thenReturn(null);
    when(requestContext.getVersion()).thenReturn("V10");

    when(version.getId()).thenReturn("V10");
    when(version.getEdmProvider()).thenReturn(edmProvider);
    when(version.getEntityManagerFactory()).thenReturn(emf);

    when(sessionContext.getApiVersion("V10")).thenReturn(version);
    when(sessionContext.getApiVersion(JPAODataApiVersionAccess.DEFAULT_VERSION)).thenReturn(null);

    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertNotNull(cut.getEdmProvider());
    assertNotNull(cut.getEntityManager());
  }

  @Test
  void testGetMappingPathNullIfNotProvided() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertNull(cut.getMappingPath());
  }

  @Test
  void testGetMappingPathReturnsProvided() {
    when(version.getMappingPath()).thenReturn("test/v1");
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
    assertEquals("test/v1", cut.getMappingPath());
  }

}