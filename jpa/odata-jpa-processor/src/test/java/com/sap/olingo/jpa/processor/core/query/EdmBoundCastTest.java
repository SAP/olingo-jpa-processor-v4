package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EdmBoundCastTest {
  private EdmBoundCast cut;
  private EdmEntityType et;
  private EdmBindingTarget bindingTarget;

  @BeforeEach
  void setup() {
    et = mock(EdmEntityType.class);
    bindingTarget = mock(EdmBindingTarget.class);
  }

  @Test
  void testGetName() {
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertEquals("Person", cut.getName());
  }

  @Test
  void testGetRelatedBindingTarget() {
    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(et.getName()).thenReturn("Person");
    when(et.getNavigationProperty("Hello")).thenReturn(navigationProperty);
    final EdmEntityType boundEt = mock(EdmEntityType.class);
    when(navigationProperty.getType()).thenReturn(boundEt);
    when(boundEt.getName()).thenReturn("Test");
    cut = new EdmBoundCast(et, bindingTarget);

    final EdmBindingTarget act = cut.getRelatedBindingTarget("Hello");

    assertEquals("Test", act.getName());
  }

  @Test
  void testGetRelatedBindingTargetExceptionOnUnknownNavigation() {
    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(et.getName()).thenReturn("Person");
    when(et.getNavigationProperty("Hello")).thenReturn(null);
    final EdmEntityType boundEt = mock(EdmEntityType.class);
    when(navigationProperty.getType()).thenReturn(boundEt);
    when(boundEt.getName()).thenReturn("Test");
    cut = new EdmBoundCast(et, bindingTarget);

    assertThrows(EdmException.class, () -> cut.getRelatedBindingTarget("Hello"));
  }

  @Test
  void testGetRelatedBindingTargetExceptionOnEntityTypeNotFound() {
    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(et.getName()).thenReturn("Person");
    when(et.getNavigationProperty("Hello")).thenReturn(navigationProperty);
    when(navigationProperty.getType()).thenReturn(null);
    cut = new EdmBoundCast(et, bindingTarget);

    assertThrows(EdmException.class, () -> cut.getRelatedBindingTarget("Hello"));
  }

  @Test
  void testGetMappingReturnsNull() {
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertNull(cut.getMapping());
  }

  @Test
  void testGetTitleReturnsNull() {
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertNull(cut.getTitle());
  }

  @Test
  void testGetEntityTypeWithAnnotations() {
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertNull(cut.getEntityTypeWithAnnotations());
  }

  @Test
  void testGetEntityContainer() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    when(bindingTarget.getEntityContainer()).thenReturn(container);
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertNotNull(cut.getEntityContainer());
  }

  @Test
  void testGetEntityContainerFromBoundNavigation() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    when(bindingTarget.getEntityContainer()).thenReturn(container);
    when(et.getName()).thenReturn("Person");

    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(et.getName()).thenReturn("Person");
    when(et.getNavigationProperty("Hello")).thenReturn(navigationProperty);
    final EdmEntityType boundEt = mock(EdmEntityType.class);
    when(navigationProperty.getType()).thenReturn(boundEt);
    when(boundEt.getName()).thenReturn("Test");

    cut = (EdmBoundCast) new EdmBoundCast(et, bindingTarget).getRelatedBindingTarget("Hello");

    assertNotNull(cut.getEntityContainer());
  }

  @Test
  void testGetEntityType() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    when(bindingTarget.getEntityContainer()).thenReturn(container);
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertEquals(et, cut.getEntityType());
  }

  @Test
  void testGetEntityTypeFromBoundNavigation() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    when(bindingTarget.getEntityContainer()).thenReturn(container);
    when(et.getName()).thenReturn("Person");

    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(et.getName()).thenReturn("Person");
    when(et.getNavigationProperty("Hello")).thenReturn(navigationProperty);
    final EdmEntityType boundEt = mock(EdmEntityType.class);
    when(navigationProperty.getType()).thenReturn(boundEt);
    when(boundEt.getName()).thenReturn("Test");

    cut = (EdmBoundCast) new EdmBoundCast(et, bindingTarget).getRelatedBindingTarget("Hello");

    assertEquals(boundEt, cut.getEntityType());
  }

  @Test
  void testGetNavigationPropertyBindingsReturnsEmptyList() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    when(bindingTarget.getEntityContainer()).thenReturn(container);
    when(et.getName()).thenReturn("Person");
    cut = new EdmBoundCast(et, bindingTarget);

    assertTrue(cut.getNavigationPropertyBindings().isEmpty());
  }

  @Test
  void testGetAnnotation() {
    final EdmTerm term = mock(EdmTerm.class);
    final EdmAnnotation exp = mock(EdmAnnotation.class);
    when(et.getName()).thenReturn("Person");
    when(et.getAnnotation(term, "test")).thenReturn(exp);
    cut = new EdmBoundCast(et, bindingTarget);

    assertEquals(exp, cut.getAnnotation(term, "test"));
  }

  @Test
  void testGetAnnotations() {
    final List<EdmAnnotation> exp = new ArrayList<>();
    when(et.getName()).thenReturn("Person");
    when(et.getAnnotations()).thenReturn(exp);
    cut = new EdmBoundCast(et, bindingTarget);

    assertEquals(exp, cut.getAnnotations());
  }
}
