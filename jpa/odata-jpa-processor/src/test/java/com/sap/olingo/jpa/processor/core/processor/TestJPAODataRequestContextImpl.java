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

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.FullNameCalculator;
import com.sap.olingo.jpa.processor.core.testobjects.HeaderParamTransientPropertyConverter;
import com.sap.olingo.jpa.processor.core.testobjects.TwoParameterTransientPropertyConverter;

class TestJPAODataRequestContextImpl {
  private JPAODataInternalRequestContext cut;

  @BeforeEach
  void setup() {
    cut = new JPAODataInternalRequestContext();
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
  void testReturnsSetPage() throws ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage exp = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setJPAODataPage(exp);
    assertEquals(exp, cut.getPage());
    assertEquals(uriInfo, cut.getUriInfo());
  }

  @Test
  void testReturnsSetUriInfo() throws ODataJPAIllegalAccessException {
    final UriInfo exp = mock(UriInfo.class);
    cut.setUriInfo(exp);
    assertEquals(exp, cut.getUriInfo());
  }

  @Test
  void testReturnsSetJPASerializer() throws ODataJPAIllegalAccessException {
    final JPASerializer exp = mock(JPASerializer.class);
    cut.setJPASerializer(exp);
    assertEquals(exp, cut.getSerializer());
  }

  @Test
  void testThrowsExceptionOnSetPageIfUriInfoExists() throws ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setUriInfo(uriInfo);
    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setJPAODataPage(page));
  }

  @Test
  void testThrowsExceptionOnPageIsNull() throws ODataJPAIllegalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setJPAODataPage(null));
  }

  @Test
  void testThrowsExceptionOnSetUriInfoIfUriInfoExists() throws ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setJPAODataPage(page);
    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.setUriInfo(uriInfo));
  }

  @Test
  void testThrowsExceptionOnUriInfoIsNull() throws ODataJPAIllegalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setUriInfo(null));
  }

  @Test
  void testThrowsExceptionOnSerializerIsNull() throws ODataJPAIllegalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setJPASerializer(null));
  }

  @Test
  void testCopyConstructorCopysExternalAndAddsUriInfo() throws ODataJPAIllegalAccessException {
    fillContextForCopyConstructor();
    final JPASerializer serializer = mock(JPASerializer.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    final Map<String, List<String>> header = Collections.emptyMap();
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(page, serializer, cut, header);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(page, act.getPage());
    assertEquals(serializer, act.getSerializer());
    assertCopied(act);
  }

  @Test
  void testCopyConstructorCopysExternalAndAddsPageSerializer() {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(uriInfo, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertCopied(act);
  }

  @Test
  void testCopyConstructorCopysExternalAndAddsUriInfoSerializer() {
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
  void testCopyConstructorCopysExternalAndAddsUriInfoSerializerNull() {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final Map<String, List<String>> header = Collections.emptyMap();
    final JPAODataInternalRequestContext act = new JPAODataInternalRequestContext(uriInfo, null, cut, header);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(null, act.getSerializer());
    assertCopied(act);
  }

  @Test
  void testGetCalculatorReturnsEmptyOptionalIfNotTransient() throws ODataJPAModelException,
      ODataJPAProcessorException {
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
        .with().setEntityManager(mock(EntityManager.class)).build());
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
    final Constructor<?> c = HeaderParamTransientPropertyConverter.class.getConstructor(Map.class);
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
        Map.class);
    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with().setEntityManager(mock(EntityManager.class)).build());
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
        .build());

    assertEquals(Locale.UK, cut.getLocale());
  }

  @Test
  void testGetLocaleReturnsValueFromExternalContextAfterCopy() {
    cut = new JPAODataInternalRequestContext(JPAODataRequestContext
        .with()
        .setEntityManager(mock(EntityManager.class))
        .setLocales(Arrays.asList(Locale.UK, Locale.ENGLISH))
        .build());

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
    cut = new JPAODataInternalRequestContext(context);
  }
}
