package com.sap.olingo.jpa.processor.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAProcessorCountAttributeImplTest extends AbstractJPAProcessorAttributeTest {

  @Override
  protected void createCutSortOrder(final boolean descending) {
    cut = new JPAProcessorCountAttributeImpl(Collections.emptyList(), descending);
  }

  @Test
  @Override
  void testJoinRequired() {
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    assertTrue(cut.requiresJoin());
  }

  @Test
  @Override
  void testJoinNotRequired() {
    cut = new JPAProcessorCountAttributeImpl(Collections.emptyList(), true);
    assertFalse(cut.requiresJoin());
  }

  @Test
  void testIsSortable() {
    final var association = mock(JPAAssociationAttribute.class);
    when(association.isTransient()).thenReturn(false);
    when(hop.getLeaf()).thenReturn(association);
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    assertTrue(cut.isSortable());
  }

  @Test
  void testNotIsSortableNoHop() {
    cut = new JPAProcessorCountAttributeImpl(Collections.emptyList(), true);
    assertFalse(cut.isSortable());
  }

  @Test
  void testNotIsSortableTransient() {
    final var association = mock(JPAAssociationAttribute.class);
    when(association.isTransient()).thenReturn(true);
    when(hop.getLeaf()).thenReturn(association);
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    assertFalse(cut.isSortable());
  }

  @Test
  void testAliasFromHop() {
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    assertEquals("hophop", cut.getAlias());
  }

  @Test
  void testAliasNoHop() {
    cut = new JPAProcessorCountAttributeImpl(Collections.emptyList(), true);
    assertEquals("Count", cut.getAlias());
  }

  @Test
  void testExceptionOnMoreThanOneHop() {
    final var hop2 = mock(JPAAssociationPath.class);
    final ODataJPAIllegalArgumentException act = assertThrows(ODataJPAIllegalArgumentException.class, // NOSONAR
        () -> new JPAProcessorCountAttributeImpl(Arrays.asList(hop, hop2), true));
    assertEquals(1, act.getParams().length);
  }

  @Test
  void testCreateJoinReturnsNullIfNotNeeded() {
    final var cb = mock(CriteriaBuilder.class);
    final var from = mock(From.class);
    cut = new JPAProcessorCountAttributeImpl(Collections.emptyList(), true);
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

    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
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

    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    cut.setTarget(from, Collections.singletonMap("hophop", join), cb);
    final var act = cut.createJoin();
    assertEquals(join, act);
    verify(from, times(0)).join(anyString(), any());
  }

  @Test
  @Override
  void testCreateJoinThrowsExceptionIfSetTargetNotCalled() {
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    assertThrows(IllegalAccessError.class, () -> cut.createJoin());
  }

  @Test
  void testOrderByOfTransientThrowsException() {
    final var cb = mock(CriteriaBuilder.class);
    final var attribute = mock(JPAAttribute.class);
    when(hop.getPath()).thenReturn(Collections.singletonList(attribute));
    when(attribute.toString()).thenReturn("Test");
    when(attribute.isTransient()).thenReturn(true);
    cut = new JPAProcessorCountAttributeImpl(Collections.singletonList(hop), true);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createOrderBy(cb, Collections.emptyList()));
    assertEquals(501, act.getStatusCode());
  }

}