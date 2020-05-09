package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

public class EntityManagerWrapperTest {

  private EntityManagerWrapper cut;
  private EntityManager em;
  private JPAServiceDocument sd;

  @BeforeEach
  public void setup() {
    em = mock(EntityManager.class);
    sd = mock(JPAServiceDocument.class);
    cut = new EntityManagerWrapper(em, sd);
  }

  static Stream<Arguments> parameterFreeMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    return Stream.of(
        arguments(c.getMethod("flush")),
        arguments(c.getMethod("getFlushMode")),
        arguments(c.getMethod("getProperties")),
        arguments(c.getMethod("joinTransaction")),
        arguments(c.getMethod("isOpen")),
        arguments(c.getMethod("getDelegate")),
        arguments(c.getMethod("close")),
        arguments(c.getMethod("getEntityManagerFactory")),
        // arguments(c.getMethod("getCriteriaBuilder")), -> create own criteria builder
        arguments(c.getMethod("isJoinedToTransaction")),
        arguments(c.getMethod("getTransaction")),
        arguments(c.getMethod("clear")),
        arguments(c.getMethod("getMetamodel")));
  }

  static Stream<Arguments> oneParameterMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    final CriteriaUpdate<?> cu = mock(CriteriaUpdate.class);
    final CriteriaDelete<?> cd = mock(CriteriaDelete.class);
    return Stream.of(
        arguments(c.getMethod("persist", Object.class), dummy),
        arguments(c.getMethod("merge", Object.class), dummy),
        arguments(c.getMethod("remove", Object.class), dummy),
        arguments(c.getMethod("setFlushMode", FlushModeType.class), FlushModeType.COMMIT),
        arguments(c.getMethod("refresh", Object.class), dummy),
        arguments(c.getMethod("detach", Object.class), dummy),
        arguments(c.getMethod("contains", Object.class), dummy),
        arguments(c.getMethod("getLockMode", Object.class), dummy),
        arguments(c.getMethod("createQuery", String.class), dummy),
        // arguments(c.getMethod("createQuery", CriteriaQuery.class), cq), -> create own query impl
        arguments(c.getMethod("createQuery", CriteriaUpdate.class), cu),
        arguments(c.getMethod("createQuery", CriteriaDelete.class), cd),
        arguments(c.getMethod("createNativeQuery", String.class), dummy),
        arguments(c.getMethod("createNamedStoredProcedureQuery", String.class), dummy),
        arguments(c.getMethod("createStoredProcedureQuery", String.class), dummy),
        arguments(c.getMethod("unwrap", Class.class), c),
        arguments(c.getMethod("createEntityGraph", Class.class), c),
        arguments(c.getMethod("createEntityGraph", String.class), dummy),
        arguments(c.getMethod("getEntityGraph", String.class), dummy),
        arguments(c.getMethod("getEntityGraphs", Class.class), c)

    );
  }

  static Stream<Arguments> twoParameterMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    return Stream.of(
        arguments(c.getMethod("find", Class.class, Object.class), c, dummy),
        arguments(c.getMethod("getReference", Class.class, Object.class), c, dummy),
        arguments(c.getMethod("lock", Object.class, LockModeType.class), dummy, LockModeType.OPTIMISTIC),
        arguments(c.getMethod("refresh", Object.class, LockModeType.class), dummy, LockModeType.OPTIMISTIC),
        arguments(c.getMethod("refresh", Object.class, Map.class), dummy, new HashMap<>()),
        arguments(c.getMethod("setProperty", String.class, Object.class), dummy, dummy),

        arguments(c.getMethod("createNativeQuery", String.class, Class.class), dummy, c),
        arguments(c.getMethod("createStoredProcedureQuery", String.class, Class[].class), dummy, new Class[0]),
        arguments(c.getMethod("createStoredProcedureQuery", String.class, String[].class), dummy, new String[0]));
  }

  static Stream<Arguments> nParameterMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    return Stream.of(
        arguments(c.getMethod("find", Class.class, Object.class, Map.class), c, dummy, new HashMap<>(), dummy),
        arguments(c.getMethod("find", Class.class, Object.class, LockModeType.class), c, dummy, LockModeType.NONE,
            dummy),
        arguments(c.getMethod("find", Class.class, Object.class, LockModeType.class, Map.class), c, dummy,
            LockModeType.OPTIMISTIC, new HashMap<>()),
        arguments(c.getMethod("lock", Object.class, LockModeType.class, Map.class), dummy, LockModeType.NONE,
            new HashMap<>(), dummy),
        arguments(c.getMethod("refresh", Object.class, LockModeType.class, Map.class), dummy, LockModeType.NONE,
            new HashMap<>(), dummy));
  }

  static Stream<Arguments> notImplementedMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    return Stream.of(
        arguments(c.getMethod("createNamedQuery", String.class), dummy, dummy),
        arguments(c.getMethod("createNativeQuery", String.class, String.class), dummy, dummy),
        arguments(c.getMethod("createQuery", String.class, Class.class), dummy, c),
        arguments(c.getMethod("createNamedQuery", String.class, Class.class), dummy, c));
  }

  @ParameterizedTest
  @MethodSource("parameterFreeMethod")
  public void testOriginalIsCalledNoParameter(final Method m) throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {

    if (m.getParameterCount() == 0) {
      m.invoke(cut);
      final EntityManager v = verify(em);
      m.invoke(v);
    }
  }

  @ParameterizedTest
  @MethodSource("oneParameterMethod")
  public void testOriginalIsCalledOneParameter(final Method m, final Object p)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (m.getParameterCount() == 1) {
      m.invoke(cut, p);
      final EntityManager v = verify(em);
      m.invoke(v, p);
    }
  }

  @ParameterizedTest
  @MethodSource("twoParameterMethod")
  public void testOriginalIsCalledTwoParameter(final Method m, final Object p1, final Object p2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (m.getParameterCount() == 2) {
      m.invoke(cut, p1, p2);
      final EntityManager v = verify(em);
      m.invoke(v, p1, p2);
    }
  }

  @ParameterizedTest
  @MethodSource("notImplementedMethod")
  public void testThrowsNotImplemented(final Method m, final Object p1, final Object p2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Exception e = null;
    if (m.getParameterCount() == 1) {
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut, p1));
    } else if (m.getParameterCount() == 2) {
      e = assertThrows(InvocationTargetException.class, () -> m.invoke(cut, p1, p2));
    }
    assertTrue(e.getCause() instanceof NotImplementedException);
  }

  @ParameterizedTest
  @MethodSource("nParameterMethod")
  public void testOriginalIsCalledMoreParameter(final Method m, final Object p1, final Object p2, final Object p3,
      final Object p4) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (m.getParameterCount() == 3) {
      m.invoke(cut, p1, p2, p3);
      final EntityManager v = verify(em);
      m.invoke(v, p1, p2, p3);
    } else if (m.getParameterCount() == 4) {
      m.invoke(cut, p1, p2, p3, p4);
      final EntityManager v = verify(em);
      m.invoke(v, p1, p2, p3, p4);
    }
  }
}
