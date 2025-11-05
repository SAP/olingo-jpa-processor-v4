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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;

class JPAODataDefaultTransactionFactoryTest {

  private JPAODataDefaultTransactionFactory cut;
  private EntityManager em;
  private EntityTransaction transaction;

  @BeforeEach
  void setup() {
    em = mock(EntityManager.class);
    transaction = mock(EntityTransaction.class);
    when(em.getTransaction()).thenReturn(transaction);
  }

  @Test
  void testCreateFactory() {
    cut = new JPAODataDefaultTransactionFactory(em);
    assertNotNull(cut);
  }

  @Test
  void testCreateTransaction() throws ODataJPATransactionException {

    cut = new JPAODataDefaultTransactionFactory(em);
    assertNotNull(cut.createTransaction());
  }

  @Test
  void testCreateTransactionThrowsExceptionIfActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> cut.createTransaction());
  }

  @Test
  void testCreateTransactionThrowsExceptionIfActiveThrows() throws ODataJPATransactionException {

    when(transaction.isActive()).thenThrow(IllegalStateException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, () -> cut.createTransaction());
  }

  @Test
  void testCreateTransactionCreateNewTransactionIfOldNotActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(false);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertNotNull(cut.createTransaction());
  }

  @Test
  void testIsActiveReturnFalseIfNoTransactionHasBeenCreated() {

    cut = new JPAODataDefaultTransactionFactory(em);
    assertFalse(cut.hasActiveTransaction());
  }

  @Test
  void testIsActiveReturnTrueIfTransactionHasBeenCreated() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  void testIsActiveReturnTrueIfTransactionIsActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  void testIsActiveReturnFalseIfTransactionIsNotActive() throws ODataJPATransactionException {

    when(transaction.isActive()).thenReturn(false);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertFalse(cut.hasActiveTransaction());
  }

  @Test
  void testIsActiveReturnTrueIfTransactionThrowsException() throws ODataJPATransactionException {

    when(transaction.isActive()).thenThrow(IllegalStateException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    cut.createTransaction();
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  void testIsActiveReturnTrueNotCreatedButActiveTransactionThrowsException() {

    when(transaction.isActive()).thenReturn(true);
    cut = new JPAODataDefaultTransactionFactory(em);
    assertTrue(cut.hasActiveTransaction());
  }

  @Test
  void testCommitIsCalled() throws ODataJPATransactionException {

    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.commit();
    verify(transaction, times(1)).commit();
  }

  @Test
  void testCommitRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    doThrow(RuntimeException.class).when(transaction).commit();
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, act::commit);
  }

  @Test
  void testRollbackIsCalled() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.rollback();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testRollbackRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    doThrow(RuntimeException.class).when(transaction).rollback();
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, act::rollback);
  }

  @Test
  void testRollbackOnlyIsCalled() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    act.rollbackOnly();
    verify(transaction, times(1)).getRollbackOnly();
  }

  @Test
  void testRollbackOnlyRethrowsException() throws ODataJPATransactionException {
    when(em.getTransaction()).thenReturn(transaction);
    when(transaction.getRollbackOnly()).thenThrow(RuntimeException.class);
    cut = new JPAODataDefaultTransactionFactory(em);
    final JPAODataTransaction act = cut.createTransaction();
    assertThrows(ODataJPATransactionException.class, act::rollbackOnly);
  }
}
