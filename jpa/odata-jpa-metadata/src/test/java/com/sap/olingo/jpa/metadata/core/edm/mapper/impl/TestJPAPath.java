package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MIXED_PART_OF_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerWithGroups;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public class TestJPAPath extends TestMappingRoot {
  private JPAEntityType organization;
  private JPAEntityType bupaWithGroup;
  private TestHelper helper;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    organization = new IntermediateEntityType(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper.getEntityType(
        Organization.class), helper.schema);
    bupaWithGroup = new IntermediateEntityType(new JPADefaultEdmNameBuilder(PUNIT_NAME), helper.getEntityType(
        BusinessPartnerWithGroups.class), helper.schema);
  }

  @Test
  public void checkOnePathElementAlias() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Name1");
    assertEquals("Name1", cut.getAlias());
  }

  @Test
  public void checkOnePathElementPathSize() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Name1");
    assertEquals(1, cut.getPath().size());
  }

  @Test
  public void checkOnePathElementElement() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Name1");
    assertEquals("name1", cut.getPath().get(0).getInternalName());
  }

  @Test
  public void checkOnePathElementFromSuperTypeAlias() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Type");
    assertEquals("Type", cut.getAlias());
  }

  @Test
  public void checkTwoPathElementAlias() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Address/Country");
    assertEquals("Address/Country", cut.getAlias());
  }

  @Test
  public void checkTwoPathElementPathSize() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Address/Country");
    assertEquals(2, cut.getPath().size());
  }

  @Test
  public void checkTwoPathElementPathElements() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("Address/Country");
    assertEquals("address", cut.getPath().get(0).getInternalName());
    assertEquals("country", cut.getPath().get(1).getInternalName());
  }

  @Test
  public void checkThreePathElementAlias() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals("AdministrativeInformation/Created/By", cut.getAlias());
  }

  @Test
  public void checkThreePathElementPathSize() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals(3, cut.getPath().size());
  }

  @Test
  public void checkThreePathElementPathElements() throws ODataApplicationException, ODataJPAModelException {
    JPAPath cut = organization.getPath("AdministrativeInformation/Created/By");
    assertEquals("administrativeInformation", cut.getPath().get(0).getInternalName());
    assertEquals("created", cut.getPath().get(1).getInternalName());
    assertEquals("by", cut.getPath().get(2).getInternalName());
  }

  @Test
  public void checkIsPartOfGroupReturnsTrueOnNotAnnotated() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Type");
    assertTrue(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  public void checkIsPartOfGroupReturnsTrueOnAnnotatedBelogsToIt() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
  }

  @Test
  public void checkIsPartOfGroupCheckTwice() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
    assertTrue(act.isPartOfGroups(Arrays.asList("Person")));
  }

  @Test
  public void checkIsPartOfGroupReturnsFalseOnAnnotatedDoesNotBelogsToIt() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("Country");
    assertFalse(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  public void checkIsPartOfGroupReturnsFalseOnAnnotatedComplex() throws ODataJPAModelException {

    final JPAPath act = bupaWithGroup.getPath("CommunicationData/Email");
    assertFalse(act.isPartOfGroups(Arrays.asList("Test")));

  }

  @Test
  public void checkIsPartOfGroupReturnsTrueOnNotAnnotatedComplex() throws ODataJPAModelException {

    final JPAPath act = organization.getPath("CommunicationData/Email");
    assertTrue(act.isPartOfGroups(Arrays.asList("Test")));
  }

  @Test
  public void checkThrowsExceptionOnInconsistentGroups1() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getGroups()).thenReturn(Arrays.asList("Dummy"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkThrowsExceptionOnInconsistentGroups2() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getGroups()).thenReturn(Arrays.asList("Dummy", "Willi"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkThrowsExceptionOnInconsistentGroups3() {
    final List<JPAElement> attributes = new ArrayList<>(2);

    final IntermediateProperty complex = mock(IntermediateProperty.class);
    when(complex.isPartOfGroup()).thenReturn(true);
    when(complex.getGroups()).thenReturn(Arrays.asList("Test", "Dummy"));
    attributes.add(complex);

    final IntermediateProperty primitive = mock(IntermediateProperty.class);
    when(primitive.isPartOfGroup()).thenReturn(true);
    when(primitive.getGroups()).thenReturn(Arrays.asList("Dummy", "Test", "Willi"));
    attributes.add(primitive);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAPathImpl("Communication/Email", "Telecom.Email", attributes));

    assertEquals(NOT_SUPPORTED_MIXED_PART_OF_GROUP.getKey(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }
}
