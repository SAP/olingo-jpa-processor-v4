package com.sap.olingo.jpa.metadata.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAUserGroupRestrictable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAUserGroupRestrictableTest {

  private JPAUserGroupRestrictable cut;

  @BeforeEach
  void setup() {
    cut = spy(JPAUserGroupRestrictable.class);
  }

  @Test
  void testIsAccessibleForIsTrueIfNotProtected() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of());
    assertTrue(cut.isAccessibleFor(List.of("Company")));
    assertTrue(cut.isAccessibleFor(List.of()));
  }

  @Test
  void testIsAccessibleForIsTrueIfProtected() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of("Company"));
    assertTrue(cut.isAccessibleFor(List.of("Company")));
  }

  @Test
  void testIsAccessibleForIsTrueIfProtectedInList() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of("Company", "Enterprise"));
    assertTrue(cut.isAccessibleFor(List.of("Company")));
  }

  @Test
  void testIsAccessibleForIsTrueIfProtectedInListOneMatch() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of("Company", "Enterprise"));
    assertTrue(cut.isAccessibleFor(List.of("Person", "Company")));
  }

  @Test
  void testIsAccessibleForIsFalseIfGroupsNotEq() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of("Company"));
    assertFalse(cut.isAccessibleFor(List.of("Person")));
  }

  @Test
  void testIsAccessibleForIsFalseIfGroupsNotInList() throws ODataJPAModelException {
    when(cut.getUserGroups()).thenReturn(List.of("Company", "Enterprise"));
    assertFalse(cut.isAccessibleFor(List.of("Person", "Employee")));
  }
}