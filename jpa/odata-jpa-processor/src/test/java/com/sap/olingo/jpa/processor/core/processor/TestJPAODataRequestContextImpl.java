package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.FullNameCalculator;
import com.sap.olingo.jpa.processor.core.testobjects.HeaderParamTransientPropertyConverter;
import com.sap.olingo.jpa.processor.core.testobjects.TwoParameterTransientPropertyConverter;

class TestJPAODataRequestContextImpl {
  private JPAODataInternalRequestContext cut;
  private JPAODataSessionContextAccess sessionContext;
  private JPAODataRequestContext requestContext;
  private JPAEdmProvider edmProvider;
  private OData odata;

  @BeforeEach
  void setup() throws ODataException {
    edmProvider = mock(JPAEdmProvider.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = mock(JPAODataRequestContext.class);
    odata = mock(OData.class);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    cut = new JPAODataInternalRequestContext(requestContext, sessionContext, odata);
  }

  @Test
  void testInitialEmptyClaimsProvider() {
    assertFalse(cut.getClaimsProvider().isPresent());
  }

  @Test
  void testInitialEmptyGroupsProvider() {
    assertFalse(cut.getGroupsProvider().isPresent());
  }

  @Test
  void testReturnsSetUriInfo() throws ODataJPAIllegalAccessException {
    final UriInfo exp = mock(UriInfo.class);
    cut.setUriInfo(exp);
    assertEquals(exp, cut.getUriInfo());
  }

  @Test
  void testReturnsSetJPASerializer() {
    final JPASerializer exp = mock(JPASerializer.class);
    cut.setJPASerializer(exp);
    assertEquals(exp, cut.getSerializer());
  }

  @Test
  void testThrowsExceptionOnSetUriInfoIfUriInfoExists() throws ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    cut.setUriInfo(uriInfo);
    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setUriInfo(uriInfo));
  }

  @Test
  void testThrowsExceptionOnUriInfoIsNull() {
    assertThrows(NullPointerException.class, () -> cut.setUriInfo(null));
  }

  @Test
  void testThrowsExceptionOnSerializerIsNull() {
    assertThrows(NullPointerException.class, () -> cut.setJPASerializer(null));
  }

  @Test
  void testCopyConstructorCopiesExternalAndAddsPageSerializer() throws ODataJPAProcessorException {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(uriInfo, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertCopied(act);
  }

  @Test
  void testCopyConstructorCopiesExternalAndAddsUriInfoSerializer() throws ODataJPAProcessorException {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPASerializer serializer = mock(JPASerializer.class);
    final Map<String, List<String>> header = Collections.emptyMap();
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(uriInfo, serializer, cut, header);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(serializer, act.getSerializer());
    assertEquals(header, act.getHeader());
    assertCopied(act);
  }

  @Test
  void testCopyConstructorCopiesExternalAndAddsUriInfoSerializerNull() throws ODataJPAProcessorException {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final Map<String, List<String>> header = Collections.emptyMap();
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(uriInfo, null, cut, header);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(null, act.getSerializer());
    assertCopied(act);
  }

  @Test
  void testGetCalculatorReturnsEmptyOptionalIfNotTransient() throws ODataJPAProcessorException {
    final JPAAttribute attribute = mock(JPAAttribute.class);
    when(attribute.isTransient()).thenReturn(false);
    assertFalse(cut.getCalculator(attribute).isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceNoParameter() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = FullNameCalculator.class.getConstructor();
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    assertTrue(cut.getCalculator(attribute).isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceFromCache() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = FullNameCalculator.class.getConstructor();
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getCalculator(attribute);
    assertEquals(act.get(), cut.getCalculator(attribute).get());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceEntityManager() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = DummyPropertyCalculator.class.getConstructor(EntityManager.class);

    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with().setEntityManager(mock(EntityManager.class)).build(), sessionContext, odata);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((DummyPropertyCalculator) act.get()).getEntityManager());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceHeader() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = HeaderParamTransientPropertyConverter.class.getConstructor(JPAHttpHeaderMap.class);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((HeaderParamTransientPropertyConverter) act.get()).getHeader());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceTwoParameter() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = TwoParameterTransientPropertyConverter.class.getConstructor(EntityManager.class,
        JPAHttpHeaderMap.class);
    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with().setEntityManager(mock(EntityManager.class)).build(), sessionContext, odata);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((TwoParameterTransientPropertyConverter) act.get()).getEntityManager());
    assertNotNull(((TwoParameterTransientPropertyConverter) act.get()).getHeader());
  }

  @Test
  void testGetLocaleReturnsValueFromExternalContext() {
    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with()
        .setEntityManager(mock(EntityManager.class))
        .setLocales(Arrays.asList(Locale.UK, Locale.ENGLISH))
        .build(), sessionContext, odata);

    assertEquals(Locale.UK, cut.getLocale());
  }

  @Test
  void testGetLocaleReturnsValueFromExternalContextAfterCopy() throws ODataJPAProcessorException {
    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with()
        .setEntityManager(mock(EntityManager.class))
        .setLocales(Arrays.asList(Locale.UK, Locale.ENGLISH))
        .build(), sessionContext, odata);

    cut = new JPAODataInternalRequestContext(mock(UriInfoResource.class), cut);
    assertEquals(Locale.UK, cut.getLocale());
  }

  private void assertCopied(final JPAODataInternalRequestContext act) {
    assertEquals(cut.getEntityManager(), act.getEntityManager());
    assertEquals(cut.getClaimsProvider().get(), act.getClaimsProvider().get());
    assertEquals(cut.getGroupsProvider().get(), act.getGroupsProvider().get());
  }

  private void fillContextForCopyConstructor() {
    final EntityManager expEm = mock(EntityManager.class);
    final JPAODataClaimProvider expCp = new JPAODataClaimsProvider();
    final JPAODataGroupProvider expGp = new JPAODataGroupsProvider();
    final JPAODataRequestContext context = JPAODataRequestContext
        .with()
        .setEntityManager(expEm)
        .setClaimsProvider(expCp)
        .setGroupsProvider(expGp)
        .build();
    cut = new JPAODataInternalRequestContext(context, sessionContext, odata);
  }
}
