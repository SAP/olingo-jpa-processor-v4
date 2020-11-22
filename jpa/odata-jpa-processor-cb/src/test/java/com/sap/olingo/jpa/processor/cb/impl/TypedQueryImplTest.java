package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

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
    verify(q).getFirstResult();
  }

  @Test
  void testGetFlushMode() {
    cut.getFlushMode();
    verify(q).getFlushMode();
  }

  @Test
  void testGetHints() {
    cut.getHints();
    verify(q).getHints();
  }

  @Test
  void testGetLockMode() {
    cut.getLockMode();
    verify(q).getLockMode();
  }

  @Test
  void testGetMaxResults() {
    cut.getMaxResults();
    verify(q).getMaxResults();
  }

  @Test
  void testGetParameterByPosition() {
    cut.getParameter(1);
    verify(q).getParameter(1);
  }

  @Test
  void testGetParameterByPositionType() {
    cut.getParameter(1, Integer.class);
    verify(q).getParameter(1, Integer.class);
  }

  @Test
  void testGetParameterByName() {
    cut.getParameter("Test");
    verify(q).getParameter("Test");
  }

  @Test
  void testGetParameterByNameType() {
    cut.getParameter("Test", String.class);
    verify(q).getParameter("Test", String.class);
  }

  @Test
  void testGetParameters() {
    cut.getParameters();
    verify(q).getParameters();
  }

  @Test
  void testGetParameterValueByPosition() {
    cut.getParameterValue(1);
    verify(q).getParameterValue(1);
  }

  @Test
  void testGetParameterValueByParameter() {
    @SuppressWarnings("unchecked")
    final Parameter<Long> param = mock(Parameter.class);
    cut.getParameterValue(param);
    verify(q).getParameterValue(param);
  }

  @Test
  void testGetParameterValueByName() {
    cut.getParameterValue("Test");
    verify(q).getParameterValue("Test");
  }

  @Test
  void testIsBound() {
    @SuppressWarnings("unchecked")
    final Parameter<Long> param = mock(Parameter.class);
    cut.isBound(param);
    verify(q).isBound(param);
  }

  @Test
  void testSetFirstResult() {
    assertEquals(cut, cut.setFirstResult(1));
    verify(q).setFirstResult(1);
  }

  @Test
  void testSetFlushMode() {
    assertEquals(cut, cut.setFlushMode(FlushModeType.AUTO));
    verify(q).setFlushMode(FlushModeType.AUTO);
  }

  @Test
  void testSetHint() {
    assertEquals(cut, cut.setHint("Test", "Test"));
    verify(q).setHint("Test", "Test");
  }

  @Test
  void testSetLockMode() {
    assertEquals(cut, cut.setLockMode(LockModeType.OPTIMISTIC));
    verify(q).setLockMode(LockModeType.OPTIMISTIC);
  }

  @Test
  void testSetMaxResults() {
    assertEquals(cut, cut.setMaxResults(1));
    verify(q).setMaxResults(1);
  }

  @Test
  void testSetParameterTemporalByPosition() {
    final Calendar value = new GregorianCalendar();
    assertEquals(cut, cut.setParameter(1, value, TemporalType.TIMESTAMP));
    verify(q).setParameter(1, value, TemporalType.TIMESTAMP);
  }

  @Test
  void testSetParameterTemporalDateByPosition() {
    final Date value = Date.valueOf(LocalDate.now());
    assertEquals(cut, cut.setParameter(2, value, TemporalType.DATE));
    verify(q).setParameter(2, value, TemporalType.DATE);
  }

  @Test
  void testSetParameterPosition() {
    assertEquals(cut, cut.setParameter(1, "Test"));
    verify(q).setParameter(1, "Test");
  }

  @Test
  void testSetParameterByTemporalByParameter() {
    @SuppressWarnings("unchecked")
    final Parameter<Calendar> param = mock(Parameter.class);
    final Calendar value = new GregorianCalendar();
    assertEquals(cut, cut.setParameter(param, value, TemporalType.TIMESTAMP));
    verify(q).setParameter(param, value, TemporalType.TIMESTAMP);
  }

  @Test
  void testSetParameterByTemporalDateByParameter() {
    @SuppressWarnings("unchecked")
    final Parameter<java.util.Date> param = mock(Parameter.class);
    final Date value = Date.valueOf(LocalDate.now());
    assertEquals(cut, cut.setParameter(param, value, TemporalType.DATE));
    verify(q).setParameter(param, value, TemporalType.DATE);
  }

  @Test
  void testSetParameterByValue() {
    @SuppressWarnings("unchecked")
    final Parameter<Integer> param = mock(Parameter.class);
    assertEquals(cut, cut.setParameter(param, new Integer(1)));
    verify(q).setParameter(param, new Integer(1));
  }

  @Test
  void testSetParameterByTemporalCalendarByName() {
    final Calendar value = new GregorianCalendar();
    assertEquals(cut, cut.setParameter("Test", value, TemporalType.TIME));
    verify(q).setParameter("Test", value, TemporalType.TIME);
  }

  @Test
  void testSetParameterByTemporalDateByName() {
    final Date value = Date.valueOf(LocalDate.now());
    assertEquals(cut, cut.setParameter("Test", value, TemporalType.DATE));
    verify(q).setParameter("Test", value, TemporalType.DATE);
  }

  @Test
  void testSetParameterByName() {
    assertEquals(cut, cut.setParameter("Test", "Test"));
    verify(q).setParameter("Test", "Test");
  }

  @Test
  void testUnwrap() {
    cut.unwrap(String.class);
    verify(q).unwrap(String.class);
  }
}
