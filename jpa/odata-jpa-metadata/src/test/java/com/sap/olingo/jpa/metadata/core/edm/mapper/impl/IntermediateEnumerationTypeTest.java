package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.DayOfWeek;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.EnumWithConverterError;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.FileAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.WrongMember;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.WrongType;

class IntermediateEnumerationTypeTest extends TestMappingRoot {

  private IntermediateEnumerationType cut;
  private IntermediateAnnotationInformation annotationInfo;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
  }

  @Test
  void checkCsdlEnumTypeAccessible() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    assertNotNull(cut.getEdmItem());
  }

  @Test
  void checkNameProvided() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertEquals("DayOfWeek", cut.getEdmItem().getName());
  }

  @Test
  void checkIsFlagProvidesFalse() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    assertFalse(cut.isFlags());
  }

  @Test
  void checkIsFlagProvidesTrue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertTrue(cut.isFlags());
  }

  @Test
  void checkIsFlagOnEdmTypeProvidesFalse() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertFalse(cut.getEdmItem().isFlags());
  }

  @Test
  void checkIsFlagOnEdmProvidesTrue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertTrue(cut.getEdmItem().isFlags());
  }

  @Test
  void checkUnderlyingTypeIntegerAsDefault() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  void checkUnderlyingTypeFromConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  void checkReturnsRightNumberOfMember4() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertEquals(4, cut.getEdmItem().getMembers().size());
  }

  @Test
  void checkReturnsRightNumberOfMember7() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertEquals(7, cut.getEdmItem().getMembers().size());
  }

  @Test
  void checkReturnsRightNameForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertEquals("MONDAY", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("SUNDAY", cut.getEdmItem().getMembers().get(6).getName());
  }

  @Test
  void checkReturnsRightValueForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    cut.getEdmItem();
    assertEquals("0", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("6", cut.getEdmItem().getMembers().get(6).getValue());
  }

  @Test
  void checkReturnsRightNameForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertEquals("Read", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("Delete", cut.getEdmItem().getMembers().get(3).getName());
  }

  @Test
  void checkReturnsRightValueForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    cut.getEdmItem();
    assertEquals("1", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("8", cut.getEdmItem().getMembers().get(3).getValue());
  }

  @Test
  void checkThrowsErrorOnIsFlagTrueAndNegativeValue() {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), WrongMember.class, annotationInfo);
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getEdmItem();
    });
  }

  @Test
  void checkThrowsErrorOnNotSupportedUnderlyingType() {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), WrongType.class, annotationInfo);
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getEdmItem();
    });
  }

  @Test
  void checkOrdinalMemberProvidedFromStringWOConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    assertEquals(DayOfWeek.SUNDAY, cut.enumOf("SUNDAY"));
  }

  @Test
  void checkOrdinalMemberProvidedFromNumberWOConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), DayOfWeek.class, annotationInfo);
    assertEquals(DayOfWeek.TUESDAY, cut.enumOf(1));
  }

  @Test
  void checkOrdinalMemberProvidedFromStringWithConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    assertEquals(FileAccess.Create, cut.enumOf("Create"));
  }

  @Test
  void checkOrdinalMemberProvidedFromNumberWithConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), FileAccess.class, annotationInfo);
    assertEquals(FileAccess.Write, cut.enumOf((short) 2));
  }

  @Test
  void checkValueOfRethrowsConstructorException() {
    cut = new IntermediateEnumerationType(new JPADefaultEdmNameBuilder(PUNIT_NAME), EnumWithConverterError.class,
        annotationInfo);
    assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
  }
}
