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

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

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
    cut = new EntityManagerFactoryWrapper(emf, sd, null);
  }

  static Stream<Arguments> method() throws NoSuchMethodException, SecurityException {
    final Class<EntityManagerFactory> clazz = EntityManagerFactory.class;
    final EntityGraph<?> entityGraph = mock(EntityGraph.class);
    final Query query = mock(Query.class);
    final String dummy = "Test";

    return Stream.of(
        arguments(clazz.getMethod("getMetamodel"), dummy, dummy, metamodel),
        arguments(clazz.getMethod("isOpen"), dummy, dummy, Boolean.TRUE),
        arguments(clazz.getMethod("close"), dummy, dummy, null),
        arguments(clazz.getMethod("getProperties"), dummy, dummy, properties),
        arguments(clazz.getMethod("getCache"), dummy, dummy, cache),
        arguments(clazz.getMethod("unwrap", Class.class), clazz, dummy, emf),
        arguments(clazz.getMethod("getPersistenceUnitUtil"), dummy, dummy, punitUtil),
        arguments(clazz.getMethod("addNamedQuery", String.class, Query.class), dummy, query, null),
        arguments(clazz.getMethod("addNamedEntityGraph", String.class, EntityGraph.class), dummy, entityGraph, null));
  }

  static Stream<Arguments> emWrapperMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManagerFactory> clazz = EntityManagerFactory.class;
    final String dummy = "Test";

    return Stream.of(
        arguments(clazz.getMethod("createEntityManager"), dummy, dummy),
        arguments(clazz.getMethod("createEntityManager", Map.class), new HashMap<>(), dummy),
        arguments(clazz.getMethod("createEntityManager", SynchronizationType.class), SynchronizationType.SYNCHRONIZED,
            dummy),
        arguments(clazz.getMethod("createEntityManager", SynchronizationType.class, Map.class),
            SynchronizationType.SYNCHRONIZED, new HashMap<>()));
  }

  @ParameterizedTest
  @MethodSource("method")
  void testOriginalCalled(final Method method, final Object p1, final Object p2, final Object ret)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Object result = null;
    if (method.getParameterCount() == 0) {
      result = method.invoke(cut);
      final EntityManagerFactory v = verify(emf);
      method.invoke(v);
    } else if (method.getParameterCount() == 1) {
      result = method.invoke(cut, p1);
      final EntityManagerFactory v = verify(emf);
      method.invoke(v, p1);
    } else if (method.getParameterCount() == 2) {
      result = method.invoke(cut, p1, p2);
      final EntityManagerFactory v = verify(emf);
      method.invoke(v, p1, p2);
    }
    if (ret != null) {
      assertEquals(ret, result);
    }
  }

  @ParameterizedTest
  @MethodSource("emWrapperMethod")
  void testEmWrapperCreated(final Method method, final Object p1, final Object p2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Object result = null;
    if (method.getParameterCount() == 0) {
      result = method.invoke(cut);
    } else if (method.getParameterCount() == 1) {
      result = method.invoke(cut, p1);
    } else if (method.getParameterCount() == 2) {
      result = method.invoke(cut, p1, p2);
    }
    assertTrue(result instanceof EntityManagerWrapper);
  }

  @Test
  void testCbWrapperCreated() {
    assertTrue(cut.getCriteriaBuilder() instanceof CriteriaBuilder);
  }
}
