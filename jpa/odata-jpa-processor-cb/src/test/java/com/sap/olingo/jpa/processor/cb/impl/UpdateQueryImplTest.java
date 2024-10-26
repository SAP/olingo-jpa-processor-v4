package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UpdateQueryImplTest extends BuilderBaseTest {

  private UpdateQueryImpl cut;
  private CriteriaUpdateImpl<?> updateQuery;
  private ParameterBuffer parameter;
  private EntityManager em;

  @BeforeEach
  void setup() {
    em = mock(EntityManager.class);
    updateQuery = mock(CriteriaUpdateImpl.class);
    parameter = new ParameterBuffer();
    when(updateQuery.getParameterBuffer()).thenReturn(parameter);
    cut = new UpdateQueryImpl(em, updateQuery);
  }

  static Stream<Arguments> illegalStateException() throws NoSuchMethodException, SecurityException {
    final Class<UpdateQueryImpl> c = UpdateQueryImpl.class;
    return Stream.of(
        arguments(c.getMethod("getResultList")),
        arguments(c.getMethod("getSingleResult")),
        arguments(c.getMethod("setMaxResults", int.class)),
        arguments(c.getMethod("setFirstResult", int.class)));
  }

  @ParameterizedTest
  @MethodSource("illegalStateException")
  void testThrowsNotImplemented(final Method method) throws IllegalAccessException, IllegalArgumentException {
    testIllegalStateException(method, cut);
  }

  @Test
  void testGetMaxResultsReturnsZero() {
    assertEquals(0, cut.getMaxResults());
  }

  @Test
  void testGetFirstResultReturnsZero() {
    assertEquals(0, cut.getFirstResult());
  }

  @Test
  void testUnwrap() {
    assertNotNull(cut.unwrap(Query.class));
  }

  @Test
  void testExecuteUpdate() {
    final Query query = mock(Query.class);
    parameter.addValue(10);
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.executeUpdate()).thenReturn(10);
    when(updateQuery.asSQL(any())).thenReturn(new StringBuilder("UPDATE"));

    final var result = cut.executeUpdate();

    assertEquals(10, result);
    verify(query, atLeastOnce()).setParameter(anyInt(), any());
  }

  protected void testIllegalStateException(final Method method, final Object cut) throws IllegalAccessException {
    try {
      invokeMethod(method, cut);
    } catch (final InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
      return;
    }
    fail();
  }

}
