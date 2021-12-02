package com.sap.olingo.jpa.processor.cb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.impl.EntityManagerWrapper;

class EntityManagerFactoryWrapperTest {

  private EntityManagerFactoryWrapper cut;
  private static EntityManagerFactory emf;
  private static EntityManager em;
  private JPAServiceDocument sd;

  private static Metamodel metamodel;
  private static Map<String, Object> properties;
  private static Cache cache;
  private static PersistenceUnitUtil punitUtil;

  @BeforeAll
  static void classSetup() {
    emf = mock(EntityManagerFactory.class);
    em = mock(EntityManager.class);
    metamodel = mock(Metamodel.class);
    properties = new HashMap<>();
    cache = mock(Cache.class);
    punitUtil = mock(PersistenceUnitUtil.class);
    when(emf.createEntityManager()).thenReturn(em);
    when(emf.getMetamodel()).thenReturn(metamodel);
    when(emf.isOpen()).thenReturn(true);
    when(emf.getProperties()).thenReturn(properties);
    when(emf.getCache()).thenReturn(cache);
    when(emf.getPersistenceUnitUtil()).thenReturn(punitUtil);
    when(emf.unwrap(EntityManagerFactory.class)).thenReturn(emf);
    when(em.isOpen()).thenReturn(true);
  }

  @BeforeEach
  void setup() {

    sd = mock(JPAServiceDocument.class);
    cut = new EntityManagerFactoryWrapper(emf, sd);
  }

  static Stream<Arguments> method() throws NoSuchMethodException, SecurityException {
    final Class<EntityManagerFactory> c = EntityManagerFactory.class;
    final EntityGraph<?> entityGraph = mock(EntityGraph.class);
    final Query query = mock(Query.class);
    final String dummy = "Test";

    return Stream.of(
        arguments(c.getMethod("getMetamodel"), dummy, dummy, metamodel),
        arguments(c.getMethod("isOpen"), dummy, dummy, Boolean.TRUE),
        arguments(c.getMethod("close"), dummy, dummy, null),
        arguments(c.getMethod("getProperties"), dummy, dummy, properties),
        arguments(c.getMethod("getCache"), dummy, dummy, cache),
        arguments(c.getMethod("unwrap", Class.class), c, dummy, emf),
        arguments(c.getMethod("getPersistenceUnitUtil"), dummy, dummy, punitUtil),
        arguments(c.getMethod("addNamedQuery", String.class, Query.class), dummy, query, null),
        arguments(c.getMethod("addNamedEntityGraph", String.class, EntityGraph.class), dummy, entityGraph, null));
  }

  static Stream<Arguments> emWrapperMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManagerFactory> c = EntityManagerFactory.class;
    final String dummy = "Test";

    return Stream.of(
        arguments(c.getMethod("createEntityManager"), dummy, dummy),
        arguments(c.getMethod("createEntityManager", Map.class), new HashMap<>(), dummy),
        arguments(c.getMethod("createEntityManager", SynchronizationType.class), SynchronizationType.SYNCHRONIZED,
            dummy),
        arguments(c.getMethod("createEntityManager", SynchronizationType.class, Map.class),
            SynchronizationType.SYNCHRONIZED, new HashMap<>()));
  }

  @ParameterizedTest
  @MethodSource("method")
  void testOriginalCalled(final Method m, final Object p1, final Object p2, final Object ret)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Object result = null;
    if (m.getParameterCount() == 0) {
      result = m.invoke(cut);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v);
    } else if (m.getParameterCount() == 1) {
      result = m.invoke(cut, p1);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v, p1);
    } else if (m.getParameterCount() == 2) {
      result = m.invoke(cut, p1, p2);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v, p1, p2);
    }
    if (ret != null) {
      assertEquals(ret, result);
    }
  }

  @ParameterizedTest
  @MethodSource("emWrapperMethod")
  void testEmWrapperCreated(final Method m, final Object p1, final Object p2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Object result = null;
    if (m.getParameterCount() == 0) {
      result = m.invoke(cut);
    } else if (m.getParameterCount() == 1) {
      result = m.invoke(cut, p1);
    } else if (m.getParameterCount() == 2) {
      result = m.invoke(cut, p1, p2);
    }
    assertTrue(result instanceof EntityManagerWrapper);
  }

  @Test
  void testCbWrapperCreated() {
    assertTrue(cut.getCriteriaBuilder() instanceof CriteriaBuilder);
  }
}
