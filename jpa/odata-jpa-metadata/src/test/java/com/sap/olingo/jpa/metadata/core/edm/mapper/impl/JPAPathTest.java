package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MIXED_PART_OF_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerWithGroups;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class JPAPathTest extends TestMappingRoot {
  private JPAEntityType organization;
  private JPAEntityType bupaWithGroup;
  private TestHelper helper;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    organization = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper
        .getEntityType(Organization.class), helper.schema);
    bupaWithGroup = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        helper.getEntityType(BusinessPartnerWithGroups.class), helper.schema);
  }

  @Test
  void checkOnePathElementAlias() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Name1");
    assertEquals("Name1", cut.getAlias());
  }

  @Test
  void checkOnePathElementPathSize() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Name1");
    assertEquals(1, cut.getPath().size());
  }

  @Test
  void checkOnePathElementElement() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Name1");
    assertEquals("name1", cut.getPath().get(0).getInternalName());
  }

  @Test
  void checkOnePathElementFromSuperTypeAlias() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Type");
    assertEquals("Type", cut.getAlias());
  }

  @Test
  void checkTwoPathElementAlias() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    assertEquals("Address/Country", cut.getAlias());
  }

  @Test
  void checkTwoPathElementPathSize() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    assertEquals(2, cut.getPath().size());
  }

  @Test
  void checkTwoPathElementPathElements() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    assertEquals("address", cut.getPath().get(0).getInternalName());
    assertEquals("country", cut.getPath().get(1).getInternalName());
  }

  @Test
  void checkThreePathElementAlias() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals("AdministrativeInformation/Created/By", cut.getAlias());
  }

  @Test
  void checkThreePathElementPathSize() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals(3, cut.getPath().size());
  }

  @Test
  void checkThreePathElementPathElements() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals("administrativeInformation", cut.getPath().get(0).getInternalName());
    assertEquals("created", cut.getPath().get(1).getInternalName());
    assertEquals("by", cut.getPath().get(2).getInternalName());
  }

  @Test
  void checkIsPartOfGroupReturnsTrueOnNotAnnotated() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Type");
    assertTrue(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  void checkIsPartOfGroupReturnsTrueOnAnnotatedBelongsToIt() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
  }

  @Test
  void checkIsPartOfGroupCheckTwice() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
  }

  @Test
  void checkIsPartOfGroupReturnsFalseOnAnnotatedDoesNotBelongsToIt() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertFalse(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  void checkIsPartOfGroupReturnsFalseOnAnnotatedComplex() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("CommunicationData/Email");
    assertFalse(act.isPartOfGroups(Arrays.asList("Test")));

  }

  @Test
  void checkIsPartOfGroupReturnsTrueOnNotAnnotatedComplex() throws ODataJPAModelException {

    final JPAPath act = organization.getPath("CommunicationData/Email");
    assertTrue(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  void checkThrowsExceptionOnInconsistentGroups1() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getUserGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getUserGroups()).thenReturn(Arrays.asList("Dummy"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkThrowsExceptionOnInconsistentGroups2() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getUserGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getUserGroups()).thenReturn(Arrays.asList("Dummy", "Willi"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkThrowsExceptionOnInconsistentGroups3() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getUserGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getUserGroups()).thenReturn(Arrays.asList("Dummy", "Test", "Willi"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkTwoNotEqualIfAliasNotEqual() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    final JPAPath act = new JPAPathImpl("Address", cut.getDBFieldName(), cut.getPath());
    assertNotEquals(act, cut);
  }

  @Test
  void checkTwoNotEqualIfElementListNotEqual() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    final List<JPAElement> pathList = new ArrayList<>(cut.getPath());
    pathList.remove(0);
    final JPAPath act = new JPAPathImpl("Address/Country", cut.getDBFieldName(), pathList);
    assertNotEquals(act, cut);
  }

  @Test
  void checkTwoEqualIfSame() throws ODataJPAModelException {
    final JPAPath cut = organization.getPath("Address/Country");
    assertEquals(cut, cut);
  }
}
