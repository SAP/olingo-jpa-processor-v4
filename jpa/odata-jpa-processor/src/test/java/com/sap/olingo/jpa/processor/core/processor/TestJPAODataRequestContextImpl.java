package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataDefaultTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.exception.JPAIllicalAccessException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public class TestJPAODataRequestContextImpl {
  private JPAODataRequestContextImpl cut;

  @BeforeEach
  public void setup() {
    cut = new JPAODataRequestContextImpl();
  }

  @Test
  public void testInitialEmptyClaimsProvider() {
    assertFalse(cut.getClaimsProvider().isPresent());
  }

  @Test
  public void testInitialEmptyGroupsProvider() {
    assertFalse(cut.getGroupsProvider().isPresent());
  }

  @Test
  public void testReturnsSetClaimsProvider() {
    final JPAODataClaimProvider exp = new JPAODataClaimsProvider();
    cut.setClaimsProvider(exp);
    assertEquals(exp, cut.getClaimsProvider().get());
  }

  @Test
  public void testReturnsSetGroupsProvider() {
    final JPAODataGroupProvider exp = new JPAODataGroupsProvider();
    cut.setGroupsProvider(exp);
    assertEquals(exp, cut.getGroupsProvider().get());
  }

  @Test
  public void testReturnsSetEntityManager() {
    final EntityManager exp = mock(EntityManager.class);
    cut.setEntityManager(exp);
    assertEquals(exp, cut.getEntityManager());
  }

  @Test
  public void testThrowsExceptionOnEntityManagerIsNull() throws JPAIllicalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setEntityManager(null));
  }

  @Test
  public void testReturnsSetPage() throws JPAIllicalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage exp = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setJPAODataPage(exp);
    assertEquals(exp, cut.getPage());
    assertEquals(uriInfo, cut.getUriInfo());
  }

  @Test
  public void testReturnsSetUriInfo() throws JPAIllicalAccessException {
    final UriInfo exp = mock(UriInfo.class);
    cut.setUriInfo(exp);
    assertEquals(exp, cut.getUriInfo());
  }

  @Test
  public void testReturnsSetJPASerializer() throws JPAIllicalAccessException {
    final JPASerializer exp = mock(JPASerializer.class);
    cut.setJPASerializer(exp);
    assertEquals(exp, cut.getSerializer());
  }

  @Test
  public void testThrowsExceptionOnSetPageIfUriInfoExists() throws JPAIllicalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setUriInfo(uriInfo);
    assertThrows(JPAIllicalAccessException.class, () -> cut.setJPAODataPage(page));
  }

  @Test
  public void testThrowsExceptionOnPageIsNull() throws JPAIllicalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setJPAODataPage(null));
  }

  @Test
  public void testThrowsExceptionOnSetUriInfoIfUriInfoExists() throws JPAIllicalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    cut.setJPAODataPage(page);
    assertThrows(JPAIllicalAccessException.class, () -> cut.setUriInfo(uriInfo));
  }

  @Test
  public void testThrowsExceptionOnUriInfoIsNull() throws JPAIllicalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setUriInfo(null));
  }

  @Test
  public void testThrowsExceptionOnSerializerIsNull() throws JPAIllicalAccessException {
    assertThrows(NullPointerException.class, () -> cut.setJPASerializer(null));
  }

  @Test
  public void testCopyConstructorCopysExternalAndAddsUriInfo() throws JPAIllicalAccessException {
    fillContextForCopyConstructor();
    final JPASerializer serializer = mock(JPASerializer.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPAODataPage page = new JPAODataPage(uriInfo, 0, 10, "12354");
    JPAODataRequestContextImpl act = new JPAODataRequestContextImpl(page, serializer, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(page, act.getPage());
    assertEquals(serializer, act.getSerializer());
    assertCopied(act);
  }

  @Test
  public void testCopyConstructorCopysExternalAndAddsPageSerializer() {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    JPAODataRequestContextImpl act = new JPAODataRequestContextImpl(uriInfo, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertCopied(act);
  }

  @Test
  public void testCopyConstructorCopysExternalAndAddsUriInfoSerializer() {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    final JPASerializer serializer = mock(JPASerializer.class);
    JPAODataRequestContextImpl act = new JPAODataRequestContextImpl(uriInfo, serializer, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(serializer, act.getSerializer());
    assertCopied(act);
  }

  @Test
  public void testCopyConstructorCopysExternalAndAddsUriInfoSerializerNull() {
    fillContextForCopyConstructor();
    final UriInfo uriInfo = mock(UriInfo.class);
    JPAODataRequestContextImpl act = new JPAODataRequestContextImpl(uriInfo, null, cut);

    assertEquals(uriInfo, act.getUriInfo());
    assertEquals(null, act.getSerializer());
    assertCopied(act);
  }

  @Test
  public void testReturnsDefaultTransactionFactory() throws JPAIllicalAccessException {
    final EntityManager em = mock(EntityManager.class);
    cut.setEntityManager(em);
    assertTrue(cut.getTransactionFactory() instanceof JPAODataDefaultTransactionFactory);
  }

  @Test
  public void testReturnsProvidedTransactionFactory() throws JPAIllicalAccessException {
    final JPAODataTransactionFactory exp = mock(JPAODataTransactionFactory.class);
    cut.setTransactionFactory(exp);
    assertEquals(exp, cut.getTransactionFactory());
  }

  private void assertCopied(JPAODataRequestContextImpl act) {
    assertEquals(cut.getEntityManager(), act.getEntityManager());
    assertEquals(cut.getClaimsProvider().get(), act.getClaimsProvider().get());
    assertEquals(cut.getGroupsProvider().get(), act.getGroupsProvider().get());
  }

  private void fillContextForCopyConstructor() {
    final EntityManager expEm = mock(EntityManager.class);
    final JPAODataClaimProvider expCp = new JPAODataClaimsProvider();
    final JPAODataGroupProvider expGp = new JPAODataGroupsProvider();
    cut.setEntityManager(expEm);
    cut.setClaimsProvider(expCp);
    cut.setGroupsProvider(expGp);
  }
}
