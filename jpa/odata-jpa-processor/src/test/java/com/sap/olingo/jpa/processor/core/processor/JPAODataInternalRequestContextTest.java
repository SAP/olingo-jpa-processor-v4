package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.debug.DebugSupport;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
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
  private JPAODataPage page;
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

  @BeforeEach
  void setup() throws ODataException {
    contextAccess = mock(JPAODataRequestContextAccess.class);
    requestContext = mock(JPAODataRequestContext.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    page = mock(JPAODataPage.class);
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

    when(contextAccess.getTransactionFactory()).thenReturn(transactionFactory);
    when(contextAccess.getRequestParameter()).thenReturn(customParameter);
    when(contextAccess.getEntityManager()).thenReturn(em);
    when(contextAccess.getClaimsProvider()).thenReturn(claims);
    when(contextAccess.getGroupsProvider()).thenReturn(groups);
    when(contextAccess.getProvidedLocale()).thenReturn(locales);
    when(contextAccess.getDebugger()).thenReturn(debugger);
    when(page.getUriInfo()).thenReturn(uriInfo);

    when(requestContext.getClaimsProvider()).thenReturn(claims);
    when(requestContext.getCUDRequestHandler()).thenReturn(cudHandler);
    when(requestContext.getGroupsProvider()).thenReturn(groups);
    when(requestContext.getTransactionFactory()).thenReturn(transactionFactory);
    when(requestContext.getRequestParameter()).thenReturn(customParameter);
    when(requestContext.getLocales()).thenReturn(locales);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getDebuggerSupport()).thenReturn(debugSupport);
    when(debugSupport.isUserAuthorized()).thenReturn(Boolean.TRUE);

    when(sessionContext.getDatabaseProcessor()).thenReturn(dbProcessor);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    when(sessionContext.getOperationConverter()).thenReturn(operationConverter);
  }

  @Test
  void testCreateFromContextAccessWithDebugger() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {

    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(serializer, cut.getSerializer());
    assertEquals(header, cut.getHeader());
    assertNotEquals(customParameter, cut.getRequestParameter());
    assertNotNull(cut.getCUDRequestHandler());
    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
    assertEquals(locales, cut.getProvidedLocale());
    assertEquals(page, cut.getPage());
    assertEquals(uriInfo, cut.getUriInfo());
  }

  @Test
  void testCreateFromContextUriAccessWithDebugger() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {

    cut = new JPAODataInternalRequestContext(uriInfoResource, serializer, contextAccess, header);

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
  }

  @Test
  void testCreateFromContextUriContextAccess() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {

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
  }

  @Test
  void testCreateFromContextUriContextAccessHeader() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {

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
  }

  @Test
  void testCreateFromRequestContextWithDebugSupport() throws ODataJPAIllegalAccessException {
    // contextAccess.get
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext);

    assertEquals(em, cut.getEntityManager());
    assertEquals(claims, cut.getClaimsProvider());
    assertEquals(groups, cut.getGroupsProvider());
    assertEquals(cudHandler, cut.getCUDRequestHandler());
    assertTrue(cut.getDebugSupport().isUserAuthorized());
    assertEquals(transactionFactory, cut.getTransactionFactory());
    assertEquals(locales, cut.getProvidedLocale());
    assertEquals(customParameter, cut.getRequestParameter());
  }

  @Test
  void testCreateFromContextAccessWithDebugSupport() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext);
    cut = new JPAODataInternalRequestContext(page, serializer, cut, header);
    assertTrue(cut.getDebugSupport().isUserAuthorized());
  }

  @Test
  void testGetLocaleLocaleNotEmpty() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);
    locales.add(Locale.JAPAN);
    locales.add(Locale.CANADA_FRENCH);

    assertEquals(Locale.JAPAN, cut.getLocale());
  }

  @Test
  void testGetLocaleLocaleEmpty() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertEquals(Locale.ENGLISH, cut.getLocale());
  }

  @Test
  void testGetLocaleLocaleNull() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    when(contextAccess.getProvidedLocale()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertEquals(Locale.ENGLISH, cut.getLocale());
  }

  @Test
  void testGetTransactionFactoryIfNull() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    when(contextAccess.getTransactionFactory()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertTrue(cut.getTransactionFactory() instanceof JPAODataDefaultTransactionFactory);
  }

  @Test
  void testGetDebuggerReturnsDefaultIfNullAndFalse() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    when(contextAccess.getDebugger()).thenReturn(null);
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertTrue(cut.getDebugger() instanceof JPAEmptyDebugger);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void testGetQueryEnhancement() throws ODataJPAModelException, ODataJPAProcessorException,
      ODataJPAIllegalAccessException {
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);
    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAQueryExtension extension = mock(JPAQueryExtension.class);
    when(et.getQueryExtention()).thenReturn(Optional.of(extension));
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
      ODataJPAIllegalAccessException, NoSuchMethodException, SecurityException {
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = DummyPropertyCalculator.class.getConstructor(EntityManager.class);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    assertTrue(cut.getCalculator(attribute).isPresent());

  }

  @Test
  void testSetUriInfoThrowsExceptionPageExists() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);

    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setUriInfo(uriInfo));
  }

  @Test
  void testSetJPAODataPageThrowsExceptionUriInfoExists() throws ODataJPAIllegalAccessException,
      ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(uriInfoResource, serializer, contextAccess, header);

    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setJPAODataPage(page));
  }

  @Test
  void testSetEntityManager() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    final EntityManager entityManager = mock(EntityManager.class);
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);
    cut.setEntityManager(entityManager);

    assertEquals(entityManager, cut.getEntityManager());
  }

  @Test
  void testSetSerializer() throws ODataJPAIllegalAccessException, ODataJPAProcessorException {
    final JPASerializer jpaSerializer = mock(JPASerializer.class);
    cut = new JPAODataInternalRequestContext(page, serializer, contextAccess, header);
    cut.setJPASerializer(jpaSerializer);

    assertEquals(jpaSerializer, cut.getSerializer());
  }

  @Test
  void testGetDatabaseProcessor() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext);
    assertEquals(dbProcessor, cut.getDatabaseProcessor());
  }

  @Test
  void testGetEdmProvider() throws ODataException {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext);
    assertEquals(edmProvider, cut.getEdmProvider());
  }

  @Test
  void testGetOperationConverter() {
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext);
    assertEquals(operationConverter, cut.getOperationConverter());
  }
}