package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DebugSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataExternalRequestContext.Builder;

class JPAODataExternalRequestContextTest {
  private Builder cut;

  @BeforeEach
  void setup() {
    cut = JPAODataRequestContext.with();
  }

  @Test
  void testSetClaimsProvider() {
    final JPAODataClaimProvider provider = mock(JPAODataClaimProvider.class);
    assertEquals(cut, cut.setClaimsProvider(provider));
    final JPAODataRequestContext act = cut.build();
    assertEquals(provider, act.getClaimsProvider().get());
  }

  @Test
  void testSetGroupsProvider() {
    final JPAODataGroupProvider provider = mock(JPAODataGroupProvider.class);
    assertEquals(cut, cut.setGroupsProvider(provider));
    final JPAODataRequestContext act = cut.build();
    assertEquals(provider, act.getGroupsProvider().get());
  }

  @Test
  void testSetCUDRequestHandler() {
    final JPACUDRequestHandler cudRequestHandler = mock(JPACUDRequestHandler.class);
    assertEquals(cut, cut.setCUDRequestHandler(cudRequestHandler));
    final JPAODataRequestContext act = cut.build();
    assertEquals(cudRequestHandler, act.getCUDRequestHandler());
  }

  @Test
  void testSetDebugSupport() {
    final DebugSupport debugSupport = mock(DebugSupport.class);
    assertEquals(cut, cut.setDebugSupport(debugSupport));
    final JPAODataRequestContext act = cut.build();
    assertEquals(debugSupport, act.getDebuggerSupport());
  }

  @Test
  void testSetEntityManager() {
    final EntityManager em = mock(EntityManager.class);
    assertEquals(cut, cut.setEntityManager(em));
    final JPAODataRequestContext act = cut.build();
    assertEquals(em, act.getEntityManager());
  }

  @Test
  void testSetTransactionFactory() {
    final JPAODataTransactionFactory em = mock(JPAODataTransactionFactory.class);
    assertEquals(cut, cut.setTransactionFactory(em));
    final JPAODataRequestContext act = cut.build();
    assertEquals(em, act.getTransactionFactory());
  }

  @Test
  void testSetOneParameter() {
    final String key = "MyKey";
    final Integer value = Integer.valueOf(10);
    assertEquals(cut, cut.setParameter(key, value));
    final JPAODataRequestContext act = cut.build();
    assertEquals(value, act.getRequestParameter().get(key));
    assertTrue(act.getRequestParameter().containsKey(key));
  }

  @Test
  void testSetTwoParameter() {
    final String key1 = "MyKey1";
    final Integer value1 = Integer.valueOf(10);
    assertEquals(cut, cut.setParameter(key1, value1));
    final String key2 = "MyKey2";
    final Integer value2 = Integer.valueOf(50);
    assertEquals(cut, cut.setParameter(key2, value2));

    final JPAODataRequestContext act = cut.build();
    assertEquals(value2, act.getRequestParameter().get(key2));
    assertTrue(act.getRequestParameter().containsKey(key2));
  }

  @Test
  void testReplaceParameter() {
    final String key1 = "MyKey1";
    final Integer value1 = Integer.valueOf(10);
    assertEquals(cut, cut.setParameter(key1, value1));
    final Integer value2 = Integer.valueOf(50);
    assertEquals(cut, cut.setParameter(key1, value2));

    final JPAODataRequestContext act = cut.build();
    assertEquals(value2, act.getRequestParameter().get(key1));
  }

  @Test
  void testGetLocalesNotNull() {
    assertNotNull(cut.build().getLocales());
  }

  @Test
  void testSetLocalesFromList() {
    assertEquals(cut, cut.setLocales(Arrays.asList(Locale.US, Locale.ENGLISH, Locale.CANADA_FRENCH)));
    final List<Locale> act = cut.build().getLocales();
    assertTrue(act.contains(Locale.ENGLISH));
  }

  @Test
  void testSetLocale() {
    assertEquals(cut, cut.setLocales(Locale.ENGLISH));
    final List<Locale> act = cut.build().getLocales();
    assertTrue(act.contains(Locale.ENGLISH));
  }
}
