package com.sap.olingo.jpa.processor.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAProcessorSimpleAttributeImplTest extends AbstractJPAProcessorAttributeTest {

  @Override
  protected void createCutSortOrder(final boolean descending) {
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), descending);

  }

  @Test
  @Override
  void testJoinRequired() {
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    assertTrue(cut.requiresJoin());
  }

  @Test
  @Override
  void testJoinNotRequired() {
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), true);
    assertFalse(cut.requiresJoin());
  }

  @Test
  void testIsSortable() {
    when(path.isTransient()).thenReturn(false);
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    assertTrue(cut.isSortable());
  }

  @Test
  void testNotIsSortable() {
    when(path.isTransient()).thenReturn(true);
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), true);
    assertFalse(cut.isSortable());
  }

  @Test
  void testAliasFromHop() {
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    assertEquals("hophop", cut.getAlias());
  }

  @Test
  void testAliasFromPath() {
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), true);
    assertEquals("path", cut.getAlias());
  }

  @Test
  void testExceptionOnMoreThanOneHop() {
    final var hop2 = mock(JPAAssociationPath.class);
    final ODataJPAIllegalArgumentException act = assertThrows(ODataJPAIllegalArgumentException.class, // NOSONAR
        () -> new JPAProcessorSimpleAttributeImpl(path, Arrays.asList(hop, hop2), true));
    assertEquals(1, act.getParams().length);
  }

  @Test
  void testCreateJoinReturnsNullIfNotNeeded() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), true);
    cut.setTarget(from, Collections.emptyMap(), cb);
    assertNull(cut.createJoin());
  }

  @Test
  void testCreateJoin() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("target");
    when(from.join("target", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
  }

  @Test
  void testCreateJoinTakenFromCache() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("target");
    when(hop.getPath()).thenReturn(Collections.singletonList(target));

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.singletonMap("hophop", join), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    verify(from, times(0)).join(anyString(), any());
  }

  @Test
  void testCreateJoinViaNavigation() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);

    final var path1 = mock(Join.class);
    final var path2 = mock(Join.class);
    final var join = mock(Join.class);

    final var complex1 = mock(JPAElement.class);
    final var complex2 = mock(JPAElement.class);
    final var target = mock(JPAElement.class);

    when(complex1.getInternalName()).thenReturn("first");
    when(complex2.getInternalName()).thenReturn("second");
    when(target.getInternalName()).thenReturn("target");

    when(from.join(eq("first"), any(JoinType.class))).thenReturn(path1);
    when(path1.join(eq("second"), any(JoinType.class))).thenReturn(path2);
    when(path2.join("target", JoinType.LEFT)).thenReturn(join);

    when(hop.getPath()).thenReturn(Arrays.asList(complex1, complex2, target));

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
  }

//  @Test
//  void checkFromListOrderByOuterJoinOnConditionOne() throws ODataApplicationException,
//      JPANoSelectionException {
//    final List<JPAProcessorAttribute> orderBy = new ArrayList<>();
//    buildRoleAssociationPath(orderBy);
//
//    final Map<String, From<?, ?>> act = cut.createFromClause2(orderBy, new ArrayList<>(), cut.cq, null);
//
//    @SuppressWarnings("unchecked")
//    final Root<Organization> root = (Root<Organization>) act.get(jpaEntityType.getExternalFQN()
//        .getFullQualifiedNameAsString());
//    final Set<Join<Organization, ?>> joins = root.getJoins();
//    assertEquals(1, joins.size());
//
//    for (final Join<Organization, ?> join : joins) {
//      assertNull(join.getOn());
//    }
//  }

  @Test
  @Override
  void testCreateJoinThrowsExceptionIfSetTargetNotCalled() {

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    assertThrows(IllegalAccessError.class, () -> cut.createJoin());
  }

  @Test
  void testGetPath() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    final var join = mock(Join.class);
    final var target = mock(JPAElement.class);
    when(target.getInternalName()).thenReturn("target");
    when(from.join("target", JoinType.LEFT)).thenReturn(join);
    when(hop.getPath()).thenReturn(Collections.singletonList(target));

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.emptyMap(), cb);
    assertNotNull(cut.getPath());
  }

  @Test
  void testOrderByOfTransientThrowsException() {
    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    when(path.isTransient()).thenReturn(true);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.toString()).thenReturn("Test");
    when(path.isPartOfGroups(any())).thenReturn(true);
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createOrderBy(cb, Collections.emptyList()));
    assertEquals(400, act.getStatusCode());
  }

  @Test
  void testOrderByNotInGroupsThrowsException() {
    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    when(path.isTransient()).thenReturn(false);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.toString()).thenReturn("Test");
    when(path.isPartOfGroups(any())).thenReturn(false);
    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.singletonList(hop), true);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createOrderBy(cb, Collections.emptyList()));
    assertEquals(403, act.getStatusCode());
  }

  @Test
  void testOrderByWithinGroupsOneGroup() throws ODataException {

    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    final var groups = Collections.singletonList("Person");
    final var from = mock(From.class);
    final var attributePath = mock(Path.class);
    final var order = mock(Order.class);
    when(path.isTransient()).thenReturn(false);
    when(path.getLeaf()).thenReturn(attribute);
    when(path.getPath()).thenReturn(Collections.singletonList(attribute));
    when(attribute.getInternalName()).thenReturn("test");
    when(path.isPartOfGroups(groups)).thenReturn(true);
    when(from.get("test")).thenReturn(attributePath);
    when(cb.desc(any())).thenReturn(order);
    when(cb.asc(any())).thenReturn(order);

    cut = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), true);
    cut.setTarget(from, Collections.emptyMap(), cb);
    final var act = cut.createOrderBy(cb, groups);
    assertNotNull(act);
  }
}
