package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class EntityManagerWrapperTest {

  private EntityManagerWrapper cut;
  private JPAServiceDocument sd;

  private static EntityManagerFactory emf;
  private static EntityManager em;
  private static Person entity;
  private static Query query;
  private static CriteriaUpdate<?> cu;
  private static CriteriaDelete<?> cd;
  private static EntityGraph<?> graph;
  private static List<EntityGraph<? super EntityManager>> graphs;
  private static StoredProcedureQuery storedProcedure;
  private static Map<String, Object> properties;
  private static EntityTransaction transaction;
  private static Metamodel metamodel;
  private static TypedQuery<String> typedQuery;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @BeforeAll
  static void classSetup() {
    entity = new Person();
    emf = mock(EntityManagerFactory.class);
    em = mock(EntityManager.class);
    query = mock(Query.class);
    typedQuery = mock(TypedQuery.class);
    cu = mock(CriteriaUpdate.class);
    cd = mock(CriteriaDelete.class);
    graph = mock(EntityGraph.class);
    graphs = new ArrayList<>();
    graphs.add((EntityGraph<? super EntityManager>) graph);
    properties = new HashMap<>();
    transaction = mock(EntityTransaction.class);
    metamodel = mock(Metamodel.class);

    when(em.merge(entity)).thenReturn(entity);
    when(em.contains(entity)).thenReturn(false);
    when(em.getLockMode(entity)).thenReturn(LockModeType.OPTIMISTIC);
    when(em.createQuery(anyString())).thenReturn(query);
    when(em.createQuery(cu)).thenReturn(query);
    when(em.createQuery(cd)).thenReturn(query);
    when(em.createNamedQuery(anyString())).thenReturn(query);
    when(em.createNamedQuery(anyString(), eq(String.class))).thenReturn(typedQuery);
    when(em.unwrap(EntityManager.class)).thenReturn(em);
    when(em.isJoinedToTransaction()).thenReturn(false);
    when(em.createEntityGraph(EntityManager.class)).thenReturn((EntityGraph<EntityManager>) graph);
    when(em.createEntityGraph("Test")).thenReturn((EntityGraph) graph);
    when(em.getEntityGraphs(EntityManager.class)).thenReturn(graphs);
    when(em.getEntityGraph("Test")).thenReturn((EntityGraph) graph);
    when(em.createNamedStoredProcedureQuery("Test")).thenReturn(storedProcedure);
    when(em.createStoredProcedureQuery("Test")).thenReturn(storedProcedure);
    when(em.getFlushMode()).thenReturn(FlushModeType.COMMIT);
    when(em.getProperties()).thenReturn(properties);
    when(em.isJoinedToTransaction()).thenReturn(true);
    when(em.getDelegate()).thenReturn("Test");
    when(em.isOpen()).thenReturn(false);
    when(em.getTransaction()).thenReturn(transaction);
    when(em.getEntityManagerFactory()).thenReturn(emf);
    when(em.getMetamodel()).thenReturn(metamodel);
    when(em.find(EntityManager.class, "Test")).thenReturn(em);
    when(em.getReference(EntityManager.class, "Test")).thenReturn(em);
    when(em.createNativeQuery("Test", EntityManager.class)).thenReturn(query);
    when(em.createStoredProcedureQuery("Test", new Class[0])).thenReturn(storedProcedure);
    when(em.createStoredProcedureQuery("Test", new String[0])).thenReturn(storedProcedure);
  }

  @BeforeEach
  void setup() {
    sd = mock(JPAServiceDocument.class);
    cut = new EntityManagerWrapper(em, sd);
  }

  static Stream<Arguments> parameterFreeMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    return Stream.of(
        arguments(c.getMethod("flush"), null),
        arguments(c.getMethod("getFlushMode"), FlushModeType.COMMIT),
        arguments(c.getMethod("getProperties"), properties),
        arguments(c.getMethod("joinTransaction"), null),
        arguments(c.getMethod("isOpen"), false),
        arguments(c.getMethod("getDelegate"), "Test"),
        arguments(c.getMethod("close"), null),
        arguments(c.getMethod("getEntityManagerFactory"), emf),
        arguments(c.getMethod("isJoinedToTransaction"), true),
        arguments(c.getMethod("getTransaction"), transaction),
        arguments(c.getMethod("clear"), null),
        arguments(c.getMethod("getMetamodel"), metamodel));
  }

  static Stream<Arguments> oneParameterMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    return Stream.of(
        arguments(c.getMethod("persist", Object.class), dummy, null),
        arguments(c.getMethod("merge", Object.class), entity, entity),
        arguments(c.getMethod("remove", Object.class), dummy, null),
        arguments(c.getMethod("setFlushMode", FlushModeType.class), FlushModeType.COMMIT, null),
        arguments(c.getMethod("refresh", Object.class), dummy, null),
        arguments(c.getMethod("detach", Object.class), dummy, null),
        arguments(c.getMethod("contains", Object.class), entity, false),
        arguments(c.getMethod("getLockMode", Object.class), entity, LockModeType.OPTIMISTIC),
        arguments(c.getMethod("createQuery", String.class), dummy, query),
        arguments(c.getMethod("createQuery", CriteriaUpdate.class), cu, query),
        arguments(c.getMethod("createQuery", CriteriaDelete.class), cd, query),
        arguments(c.getMethod("createNativeQuery", String.class), dummy, null),
        arguments(c.getMethod("createNamedQuery", String.class), dummy, query),
        arguments(c.getMethod("createNamedStoredProcedureQuery", String.class), dummy, storedProcedure),
        arguments(c.getMethod("createStoredProcedureQuery", String.class), dummy, storedProcedure),
        arguments(c.getMethod("unwrap", Class.class), c, em),
        arguments(c.getMethod("createEntityGraph", Class.class), c, graph),
        arguments(c.getMethod("createEntityGraph", String.class), dummy, graph),
        arguments(c.getMethod("getEntityGraph", String.class), dummy, graph),
        arguments(c.getMethod("getEntityGraphs", Class.class), c, graphs)

    );
  }

  static Stream<Arguments> twoParameterMethod() throws NoSuchMethodException, SecurityException {
    final Class<EntityManager> c = EntityManager.class;
    final String dummy = "Test";
    return Stream.of(
        arguments(c.getMethod("find", Class.class, Object.class), c, dummy, em),
        arguments(c.getMethod("getReference", Class.class, Object.class), c, dummy, em),
        arguments(c.getMethod("lock", Object.class, LockModeType.class), dummy, LockModeType.OPTIMISTIC, null),
        arguments(c.getMethod("refresh", Object.class, LockModeType.class), dummy, LockModeType.OPTIMISTIC, null),
        arguments(c.getMethod("refresh", Object.class, Map.class), dummy, new HashMap<>(), null),
        arguments(c.getMethod("setProperty", String.class, Object.class), dummy, dummy, null),
        arguments(c.getMethod("createNativeQuery", String.class, Class.class), dummy, c, query),
        arguments(c.getMethod("createNamedQuery", String.class, Class.class), dummy, String.class, typedQuery),
        arguments(c.getMethod("createStoredProcedureQuery", String.class, Class[].class), dummy, new Class[0],
            storedProcedure),
        arguments(c.getMethod("createStoredProcedureQuery", String.class, String[].class), dummy, new String[0],
            storedProcedure));
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
        arguments(c.getMethod("createNativeQuery", String.class, String.class), dummy, dummy),
        arguments(c.getMethod("createQuery", String.class, Class.class), dummy, c));
  }

  @ParameterizedTest
  @MethodSource("parameterFreeMethod")
  void testOriginalIsCalledNoParameter(final Method m, final Object r) throws IllegalAccessException,
      IllegalArgumentException,
      InvocationTargetException {

    if (m.getParameterCount() == 0) {
      final Object response = m.invoke(cut);
      final EntityManager v = verify(em);
      m.invoke(v);
      if (r != null)
        assertEquals(r, response);
    }
  }

  @ParameterizedTest
  @MethodSource("oneParameterMethod")
  void testOriginalIsCalledOneParameter(final Method m, final Object p, final Object r)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (m.getParameterCount() == 1) {
      final Object response = m.invoke(cut, p);
      final EntityManager v = verify(em);
      m.invoke(v, p);
      if (r != null)
        assertEquals(r, response);
    }
  }

  @ParameterizedTest
  @MethodSource("twoParameterMethod")
  void testOriginalIsCalledTwoParameter(final Method m, final Object p1, final Object p2, final Object r)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (m.getParameterCount() == 2) {
      final Object response = m.invoke(cut, p1, p2);
      final EntityManager v = verify(em);
      m.invoke(v, p1, p2);
      if (r != null)
        assertEquals(r, response);
    }
  }

  @ParameterizedTest
  @MethodSource("notImplementedMethod")
  void testThrowsNotImplemented(final Method m, final Object p1, final Object p2)
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
  void testOriginalIsCalledMoreParameter(final Method m, final Object p1, final Object p2, final Object p3,
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

  @Test
  void testGetCriteriaBuilderThrowsExceptionOnClosed() {
    when(em.isOpen()).thenReturn(false);
    assertThrows(IllegalStateException.class, () -> cut.getCriteriaBuilder());
  }
}
