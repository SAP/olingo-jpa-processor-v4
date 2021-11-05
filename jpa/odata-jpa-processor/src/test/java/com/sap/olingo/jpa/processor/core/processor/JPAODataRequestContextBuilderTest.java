package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataExternalRequestContext.Builder;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;

class JPAODataRequestContextBuilderTest {
  private static final String PARAMETER_VALUE = "Test";
  private static final String PARAMETER_NAME = "MyParameter";
  private Builder cut;

  @BeforeEach
  void setup() {
    cut = JPAODataRequestContext.with();
  }

  @Test
  void testCreateWithSetDebugSupport() {
    final DefaultDebugSupport exp = new DefaultDebugSupport();
    cut.setDebugSupport(exp);
    assertEquals(exp, cut.build().getDebuggerSupport());
  }

  @Test
  void testCreateWithSetClaimsProvider() {
    final JPAODataClaimProvider exp = new JPAODataClaimsProvider();
    cut.setClaimsProvider(exp);
    final JPAODataRequestContext act = cut.build();
    assertTrue(act.getClaimsProvider().isPresent());
    assertEquals(exp, act.getClaimsProvider().get());
  }

  @Test
  void testCreateWithSetGroupsProvider() {
    final JPAODataGroupProvider exp = new JPAODataGroupsProvider();
    cut.setGroupsProvider(exp);
    final JPAODataRequestContext act = cut.build();
    assertTrue(act.getGroupsProvider().isPresent());
    assertEquals(exp, act.getGroupsProvider().get());
  }

  @Test
  void testCreateWithSetTransactionFactory() {
    final EntityManager em = mock(EntityManager.class);
    final JPAODataTransactionFactory exp = new JPAODataDefaultTransactionFactory(em);

    cut.setTransactionFactory(exp);
    final JPAODataRequestContext act = cut.build();

    assertEquals(exp, act.getTransactionFactory());
  }

  @Test
  void testCreateWithSetEntityManager() {
    final EntityManager exp = mock(EntityManager.class);

    cut.setEntityManager(exp);
    final JPAODataRequestContext act = cut.build();

    assertEquals(exp, act.getEntityManager());
  }

  @Test
  void testThrowsExceptionOnEntityManagerIsNull() throws ODataJPAIllegalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setEntityManager(null));
  }

  @Test
  void testCreateWithSetCUDRequestHandler() {
    final JPACUDRequestHandler exp = mock(JPACUDRequestHandler.class);

    cut.setCUDRequestHandler(exp);
    final JPAODataRequestContext act = cut.build();

    assertEquals(exp, act.getCUDRequestHandler());
  }

  @Test
  void testCreateWithSetCustomParameter() {
    cut.setParameter(PARAMETER_NAME, PARAMETER_VALUE);
    final JPAODataRequestContext act = cut.build();
    assertEquals(PARAMETER_VALUE, act.getRequestParameter().get(PARAMETER_NAME));
  }
}
