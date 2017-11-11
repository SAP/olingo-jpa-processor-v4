package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.DayOfWeek;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.FileAccess;

public class TestIntermediateEnumerationType extends TestMappingRoot {

  private IntermediateEnumerationType cut;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void checkCsdlEnumTypeAccessable() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    assertNotNull(cut.getEdmItem());
  }

  @Test
  public void checkNameProvided() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("DayOfWeek", cut.getEdmItem().getName());
  }

  @Test
  public void checkIsFlagProvidesFalse() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertFalse(cut.getEdmItem().isFlags());
  }

  @Test
  public void checkIsFlagProvidesTrue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertTrue(cut.getEdmItem().isFlags());
  }

  @Test
  public void checkUnderlyingTypeIntAsDefault() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  public void checkUnderlyingTypeFromConverter() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals(EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString(), cut.getEdmItem()
        .getUnderlyingType());
  }

  @Test
  public void checkReturnsRightNumberOfMember4() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals(4, cut.getEdmItem().getMembers().size());
  }

  @Test
  public void checkReturnsRightNumberOfMember7() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals(7, cut.getEdmItem().getMembers().size());
  }

  @Test
  public void checkReturnsRightNameForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("MONDAY", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("SUNDAY", cut.getEdmItem().getMembers().get(6).getName());
  }

  @Test
  public void checkReturnsRightValueForMembersOfDayOfWeek() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
    assertEquals("0", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("6", cut.getEdmItem().getMembers().get(6).getValue());
  }

  @Test
  public void checkReturnsRightNameForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals("Read", cut.getEdmItem().getMembers().get(0).getName());
    assertEquals("Delete", cut.getEdmItem().getMembers().get(3).getName());
  }

  @Test
  public void checkReturnsRightValueForMembersOfFileAccess() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    assertEquals("1", cut.getEdmItem().getMembers().get(0).getValue());
    assertEquals("8", cut.getEdmItem().getMembers().get(3).getValue());
  }

  @Test
  public void checkThrowsErrorOnIsFlagTrueAndNegativeValue() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    fail();
  }

  @Test
  public void checkThrowsErrorOnNotSupportedUnderlyingType() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), FileAccess.class);
    cut.getEdmItem();
    fail();
  }
//  @Test
//  public void checkOrdinalMemberProvided() throws ODataJPAModelException {
//    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
//    cut.getEdmItem();
//    cut.getEdmItem().getName();
//  }
}
