package com.sap.olingo.jpa.processor.cb.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.impl.EntityManagerWrapper;

public class EntityManagerFactoryWrapperTest {

  private EntityManagerFactoryWrapper cut;
  private EntityManagerFactory emf;
  private JPAServiceDocument sd;

  @BeforeEach
  public void setup() {
    emf = mock(EntityManagerFactory.class);
    sd = mock(JPAServiceDocument.class);
    cut = new EntityManagerFactoryWrapper(emf, sd);
  }

  static Stream<Arguments> method() throws NoSuchMethodException, SecurityException {
    final Class<EntityManagerFactory> c = EntityManagerFactory.class;
    final EntityGraph<?> entityGraph = mock(EntityGraph.class);
    final Query query = mock(Query.class);
    final String dummy = "Test";

    return Stream.of(
        arguments(c.getMethod("getMetamodel"), dummy, dummy),
        arguments(c.getMethod("isOpen"), dummy, dummy),
        arguments(c.getMethod("close"), dummy, dummy),
        arguments(c.getMethod("getProperties"), dummy, dummy),
        arguments(c.getMethod("getCache"), dummy, dummy),
        arguments(c.getMethod("unwrap", Class.class), c, dummy),
        arguments(c.getMethod("getPersistenceUnitUtil"), dummy, dummy),
        arguments(c.getMethod("addNamedQuery", String.class, Query.class), dummy, query),
        arguments(c.getMethod("addNamedEntityGraph", String.class, EntityGraph.class), dummy, entityGraph));
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
  public void testOriginalCalled(final Method m, final Object p1, final Object p2)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    if (m.getParameterCount() == 0) {
      m.invoke(cut);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v);
    } else if (m.getParameterCount() == 1) {
      m.invoke(cut, p1);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v, p1);
    } else if (m.getParameterCount() == 2) {
      m.invoke(cut, p1, p2);
      final EntityManagerFactory v = verify(emf);
      m.invoke(v, p1, p2);
    }
  }

  @ParameterizedTest
  @MethodSource("emWrapperMethod")
  public void testEmWrapperCreated(final Method m, final Object p1, final Object p2)
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
  public void testCbWrapeerCreated() {
    assertTrue(cut.getCriteriaBuilder() instanceof CriteriaBuilder);
  }
}
