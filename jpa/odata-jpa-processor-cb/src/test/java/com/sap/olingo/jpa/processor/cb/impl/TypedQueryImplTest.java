package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypedQueryImplTest extends BuilderBaseTest {
  private TypedQueryImpl<Long> cut;
  private EntityManager em;
  private CriteriaQueryImpl<Long> cq;
  private ParameterBuffer parameterBuffer;
  private Query q;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    em = mock(EntityManager.class);
    cq = mock(CriteriaQueryImpl.class);
    parameterBuffer = new ParameterBuffer();
    q = mock(Query.class);
    when(cq.asSQL(any())).thenReturn(new StringBuilder().append("Test"));
    when(em.createNativeQuery("Test")).thenReturn(q);
    cut = new TypedQueryImpl<>(cq, em, parameterBuffer);
  }

  @Test
  void testExecuteUpdate() {
    cut.executeUpdate();
    verify(q).executeUpdate();
  }

  @Test
  void testGetFirstResult() {
    cut.getFirstResult();
    verify(cq).getFirstResult();
  }

  @Test
  void testGetParameterByPosition() {
    final var exp = parameterBuffer.addValue("Test");
    final var act = cut.getParameter(1);
    assertEquals(exp, act);
  }

  @Test
  void testGetParameterByPositionThrowsIfNoParameterAtPosition() {
    parameterBuffer.addValue("Test");
    assertThrows(IllegalArgumentException.class, () -> cut.getParameter(2));
  }

  @Test
  void testGetParameterByPositionType() {
    final var exp = parameterBuffer.addValue(Integer.valueOf(100));
    final var act = cut.getParameter(1, Integer.class);
    assertEquals(exp, act);
  }

  @Test
  void testGetParameterByPositionTypeThrowsIfNoParameterAtPosition() {
    parameterBuffer.addValue(Integer.valueOf(100));
    assertThrows(IllegalArgumentException.class, () -> cut.getParameter(2, Integer.class));
  }

  @Test
  void testGetParameterByPositionTypeThrowsWrongType() {
    parameterBuffer.addValue(Integer.valueOf(100));
    assertThrows(IllegalArgumentException.class, () -> cut.getParameter(1, String.class));
  }

  @Test
  void testGetParameterByName() {
    final var exp = parameterBuffer.addValue(Integer.valueOf(100));
    assertEquals(exp, cut.getParameter("1"));
  }

  @Test
  void testGetParameterByNameThrowsIfNameNotANumber() {
    parameterBuffer.addValue(Integer.valueOf(100));
    assertThrows(IllegalArgumentException.class, () -> cut.getParameter("1.2"));
  }

  @Test
  void testGetParameterByNameType() {
    final var exp = parameterBuffer.addValue(Integer.valueOf(100));
    assertEquals(exp, cut.getParameter("1", Integer.class));
  }

  @Test
  void testGetParameterByNameTypeThrowsWrongType() {
    parameterBuffer.addValue(Integer.valueOf(100));
    assertThrows(IllegalArgumentException.class, () -> cut.getParameter("1", String.class));
  }

  @Test
  void testGetParameters() {
    final var exp = parameterBuffer.addValue(Integer.valueOf(100));
    final var act = cut.getParameters();
    assertEquals(1, act.size());
    assertTrue(act.contains(exp));
  }

  @Test
  void testGetParameterValueByPosition() {
    final var exp = parameterBuffer.addValue("Test");
    final var act = cut.getParameterValue(1);
    assertEquals(exp, act);
  }

  @Test
  void testGetParameterValueByPositionFails() {
    parameterBuffer.addValue("Test");
    assertThrows(IllegalArgumentException.class, () -> cut.getParameterValue(2));
  }

  @Test
  void testGetParameterValueByParameter() {
    final var exp = parameterBuffer.addValue("Test");
    parameterBuffer.addValue(Integer.valueOf(100));
    assertEquals("Test", cut.getParameterValue(exp));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetParameterValueByParameterThrowsIfParameterNotExists() {
    final var dummy = mock(Parameter.class);
    parameterBuffer.addValue(Integer.valueOf(100));
    assertThrows(IllegalArgumentException.class, () -> cut.getParameterValue(dummy));
  }

  @Test
  void testGetParameterValueByName() {
    parameterBuffer.addValue(Integer.valueOf(100));
    assertEquals(100, cut.getParameterValue("1"));
  }

  @Test
  void testIsBound() {
    final var act = parameterBuffer.addValue(Integer.valueOf(10));
    assertTrue(cut.isBound(act));
  }

  @Test
  void testSetFirstResult() {
    assertEquals(cut, cut.setFirstResult(1));
    verify(cq).setFirstResult(1);
  }

  @Test
  void testSetGetFlushMode() {
    assertEquals(cut, cut.setFlushMode(FlushModeType.AUTO));
    assertEquals(FlushModeType.AUTO, cut.getFlushMode());
  }

  @Test
  void testSetGetHint() {
    assertEquals(cut, cut.setHint("Test", "Test"));
    final var act = cut.getHints();
    assertEquals(1, act.size());
    assertEquals("Test", act.get("Test"));
  }

  @Test
  void testSetGetLockMode() {
    assertEquals(cut, cut.setLockMode(LockModeType.OPTIMISTIC));
    assertEquals(LockModeType.OPTIMISTIC, cut.getLockMode());
  }

  @Test
  void testSetMaxResults() {
    assertEquals(cut, cut.setMaxResults(1));
    verify(cq).setMaxResults(1);
  }

  @Test
  void testGetMaxResults() {
    cut.getMaxResults();
    verify(cq).getMaxResults();
  }

  @Test
  void testSetParameterTemporalByPosition() {
    final Calendar value = new GregorianCalendar();
    assertThrows(IllegalStateException.class, () -> cut.setParameter(1, value, TemporalType.TIMESTAMP));
  }

  @Test
  void testSetParameterTemporalDateByPosition() {
    final Date value = Date.valueOf(LocalDate.now());
    assertThrows(IllegalStateException.class, () -> cut.setParameter(2, value, TemporalType.DATE));
  }

  @Test
  void testSetParameterPosition() {
    assertThrows(IllegalStateException.class, () -> cut.setParameter(1, "Test"));
  }

  @Test
  void testSetParameterByTemporalByParameter() {
    @SuppressWarnings("unchecked")
    final Parameter<Calendar> param = mock(Parameter.class);
    final Calendar value = new GregorianCalendar();
    assertThrows(IllegalStateException.class, () -> cut.setParameter(param, value, TemporalType.TIMESTAMP));
  }

  @Test
  void testSetParameterByTemporalDateByParameter() {
    @SuppressWarnings("unchecked")
    final Parameter<java.util.Date> param = mock(Parameter.class);
    final Date value = Date.valueOf(LocalDate.now());
    assertThrows(IllegalStateException.class, () -> cut.setParameter(param, value, TemporalType.DATE));
  }

  @Test
  void testSetParameterByValue() {
    @SuppressWarnings("unchecked")
    final Parameter<Integer> param = mock(Parameter.class);
    assertThrows(IllegalStateException.class, () -> cut.setParameter(param, 1));
  }

  @Test
  void testSetParameterByTemporalCalendarByName() {
    final Calendar value = new GregorianCalendar();
    assertThrows(IllegalStateException.class, () -> cut.setParameter("Test", value, TemporalType.TIME));
  }

  @Test
  void testSetParameterByTemporalDateByName() {
    final Date value = Date.valueOf(LocalDate.now());
    assertThrows(IllegalStateException.class, () -> cut.setParameter("Test", value, TemporalType.DATE));
  }

  @Test
  void testSetParameterByName() {
    assertThrows(IllegalStateException.class, () -> cut.setParameter("Test", "Test"));
  }

  @Test
  void testUnwrapThrowsException() {
    assertThrows(PersistenceException.class, () -> cut.unwrap(String.class));
  }

  @Test
  void testUnwrap() {
    assertNotNull(cut.unwrap(Query.class));
  }
}
