package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;

public class JPAODataDefaultTransactionFactoryTest {

  private JPAODataDefaultTransactionFactory cut;
  private EntityManager em;
  private EntityTransaction transaction;

  @BeforeEach
  public void setup() {
    em = mock(EntityManager.class);
    transaction = mock(EntityTransaction.class);
    when(em.getTransaction()).thenReturn(transaction);
  }

  @Test
  public void testCreateFactory() {
    cut = new JPAODataDefaultTransactionFactory(em);
    assertNotNull(cut);
  }

  @Test
  public void testCreateTransaction() throws ODataJPATransactionException {

    cut = new JPAODataDefaultTransactionFactory(em);
    assertNotNull(cut.createTransaction());
  }

  @Test
  public void testCreateTransactionThrowsExceptionIfActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> cut.createTransaction());
  }

  @Test
  public void testCreateTransactionThrowsExceptionIfActiveThrows() throws ODataJPATransactionException {

    when(transaction.isActive()).thenThrow(IllegalStateException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> cut.createTransaction());
  }

  @Test
  public void testCreateTransactionCreateNewTransactionIfOldNotActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(false);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertNotNull(cut.createTransaction());
  }

  @Test
  public void testIsActiveReturnFalseIfNoTransactionHasBeenCreated() throws ODataJPATransactionException {

    cut = new JPAODataDefaultTransactionFactory(em);
    assertFalse(cut.hasActiveTransaction());
  }

  @Test
  public void testIsActiveReturnTrueIfTransactionHasBeenCreated() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  public void testIsActiveReturnTrueIfTransactionIsActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  public void testIsActiveReturnFalseIfTransactionIsNotActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(false);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertFalse(cut.hasActiveTransaction());
  }

  @Test
  public void testIsActiveReturnTrueIfTransactionThrowsException() throws ODataJPATransactionException {

    when(transaction.isActive()).thenThrow(IllegalStateException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  public void testIsActiveReturnTrueNotCreatedButActiveTransactionThrowsException()
      throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  public void testCommitIsCalled() throws ODataJPATransactionException {

    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.commit();
    verify(transaction, times(1)).commit();
  }

  @Test
  public void testCommitRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    doThrow(RuntimeException.class).when(transaction).commit();
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> act.commit());
  }

  @Test
  public void testRollbackIsCalled() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.rollback();
    verify(transaction, times(1)).rollback();
  }

  @Test
  public void testRollbackRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    doThrow(RuntimeException.class).when(transaction).rollback();
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> act.rollback());
  }

  @Test
  public void testRollbackOnlyIsCalled() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.rollbackOnly();
    verify(transaction, times(1)).getRollbackOnly();
  }

  @Test
  public void testRollbackOnlyRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    when(transaction.getRollbackOnly()).thenThrow(RuntimeException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> act.rollbackOnly());
  }
}
