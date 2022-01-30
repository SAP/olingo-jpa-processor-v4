package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.DayOfWeek;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.FileAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.WrongMember;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.WrongType;

class TestIntermediateEnumerationType extends TestMappingRoot {

  private IntermediateEnumerationType cut;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void checkCsdlEnumTypeAccessible() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    assertNotNull(cut.getEdmItem());
  }

  @Test
  void checkNameProvided() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("DayOfWeek", cut.getEdmItem().getName());
  }

  @Test
  void checkIsFlagProvidesFalse() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertFalse(cut.getEdmItem().isFlags());
  }

  @Test
  void checkIsFlagProvidesTrue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertTrue(cut.getEdmItem().isFlags());
  }

  @Test
  void checkUnderlyingTypeIntAsDefault() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  void checkUnderlyingTypeFromConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  void checkReturnsRightNumberOfMember4() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals(4, cut.getEdmItem().getMembers().size());
  }

  @Test
  void checkReturnsRightNumberOfMember7() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals(7, cut.getEdmItem().getMembers().size());
  }

  @Test
  void checkReturnsRightNameForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("MONDAY", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("SUNDAY", cut.getEdmItem().getMembers().get(6).getName());
  }

  @Test
  void checkReturnsRightValueForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("0", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("6", cut.getEdmItem().getMembers().get(6).getValue());
  }

  @Test
  void checkReturnsRightNameForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals("Read", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("Delete", cut.getEdmItem().getMembers().get(3).getName());
  }

  @Test
  void checkReturnsRightValueForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals("1", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("8", cut.getEdmItem().getMembers().get(3).getValue());
  }

  @Test
  void checkThrowsErrorOnIsFlagTrueAndNegativeValue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), WrongMember.class);
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getEdmItem();
    });
  }

  @Test
  void checkThrowsErrorOnNotSupportedUnderlyingType() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), WrongType.class);
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getEdmItem();
    });
  }

  @Test
  void checkOrdinalMemberProvidedFromStringWOConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    assertEquals(DayOfWeek.SUNDAY, cut.enumOf("SUNDAY"));
  }

  @Test
  void checkOrdinalMemberProvidedFromNumberWOConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    assertEquals(DayOfWeek.TUESDAY, cut.enumOf(1));
  }

  @Test
  void checkOrdinalMemberProvidedFromStringWithConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    assertEquals(FileAccess.Create, cut.enumOf("Create"));
  }

  @Test
  void checkOrdinalMemberProvidedFromNumberWithConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    assertEquals(FileAccess.Write, cut.enumOf((short) 2));
  }
}
