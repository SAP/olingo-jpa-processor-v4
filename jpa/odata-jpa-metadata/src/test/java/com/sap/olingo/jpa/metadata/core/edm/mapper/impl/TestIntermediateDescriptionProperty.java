package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssociation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Country;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class TestIntermediateDescriptionProperty extends TestMappingRoot {
  private TestHelper helper;
  private IntermediateDescriptionProperty cut;
  private JPAEdmMetadataPostProcessor processor;
  private IntermediateStructuredType<?> et;
  private JPAEdmNameBuilder nameBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    final EntityType<?> type = helper.getEntityType(BusinessPartner.class);
    et = new IntermediateEntityType<>(nameBuilder, type, helper.schema);
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
  }

  @Test
  void checkPropertyCanBeCreated() throws ODataJPAModelException {
    final EmbeddableType<?> emtype = helper.getEmbeddableType("PostalAddressData");
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(emtype, "countryName");
    assertNotNull(new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema));
  }

  @Test
  void checkGetDescriptionAttributeReturnsAttribute() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getDescriptionAttribute());
    assertEquals("name", cut.getDescriptionAttribute().getInternalName());
  }

  @Test
  void checkGetFixedValueAssignmentReturnsEmptyList() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getFixedValueAssignment());
    assertTrue(cut.getFixedValueAssignment().isEmpty());
  }

  @Test
  void checkGetFixedValueAssignmentReturnsList() throws ODataJPAModelException {
    final EntityType<?> type = helper.getEntityType(Person.class);
    et = new IntermediateEntityType<>(nameBuilder, type, helper.schema);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "locationName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.lazyBuildEdmItem();
    assertNotNull(cut.getFixedValueAssignment());
    assertEquals(2, cut.getFixedValueAssignment().size());
  }

  @Test
  void checkGetTypeReturnsDescAttributeType() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getType());
    assertEquals(String.class, cut.getType());
  }

  @Test
  void checkIsAssociationReturnsTrue() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.isAssociation());
  }

  @Test
  void checkGetLocaleFieldNameReturnsPath() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getLocaleFieldName());
    assertEquals("\"LanguageISO\"", cut.getLocaleFieldName().getDBFieldName());
  }

  @Test
  void checkGetPropertyNameOneToMany() throws ODataJPAModelException {
    createDefaultCut();
    assertEquals("CountryName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  void checkGetPropertyNameManyToMany() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "regionName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals("RegionName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  void checkGetPropertyType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        cut.getEdmItem().getType(), "Wrong type");
  }

  @Test
  void checkGetTargetEntity() throws ODataJPAModelException {
    createDefaultCut();
    final JPAStructuredType target = cut.getTargetEntity();
    assertEquals("Country", target.getExternalName());
  }

  @Test
  void checkGetPartnerNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPartner());
  }

  @Test
  void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    final IntermediatePropertyAccess property = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  void checkGetPropertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertTrue(cut.getEdmItem().isNullable());
  }

  @Test
  void checkGetPropertyMaxLength() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals(Integer.valueOf(100), cut.getEdmItem().getMaxLength());
  }

  @Test
  void checkWrongPathElementThrowsException() {

    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final EdmDescriptionAssociation association = prepareCheckPath(jpaAttribute);

    final EdmDescriptionAssociation.valueAssignment[] valueAssignments =
        new EdmDescriptionAssociation.valueAssignment[1];
    final EdmDescriptionAssociation.valueAssignment valueAssignment = mock(
        EdmDescriptionAssociation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationData/dummy");
    when(association.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
      cut.getEdmItem();
    } catch (final ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @Test
  void checkWrongPathStartThrowsException() {

    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final EdmDescriptionAssociation association = prepareCheckPath(jpaAttribute);

    final EdmDescriptionAssociation.valueAssignment[] valueAssignments =
        new EdmDescriptionAssociation.valueAssignment[1];
    final EdmDescriptionAssociation.valueAssignment valueAssignment = mock(
        EdmDescriptionAssociation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationDummy/dummy");
    when(association.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
      cut.getEdmItem();
    } catch (final ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private EdmDescriptionAssociation prepareCheckPath(final Attribute<?, ?> jpaAttribute) {
    final AnnotatedMember jpaField = mock(AnnotatedMember.class);
    final ManagedType jpaManagedType = mock(ManagedType.class);
    final EdmDescriptionAssociation association = mock(EdmDescriptionAssociation.class);

    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<BusinessPartner>>() {
      @Override
      public Class<BusinessPartner> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    when(jpaAttribute.getJavaMember()).thenReturn(jpaField);
    when(jpaAttribute.getName()).thenReturn("dummy");
    when(jpaAttribute.getDeclaringType()).thenReturn(jpaManagedType);
    when(jpaManagedType.getJavaType()).thenReturn(BusinessPartner.class);

    when(jpaField.getAnnotation(EdmDescriptionAssociation.class)).thenReturn(association);

    when(association.descriptionAttribute()).thenReturn("country");
    when(association.languageAttribute()).thenReturn("language");
    when(association.localeAttribute()).thenReturn("");
    return association;
  }

  @Test
  void checkAnnotations() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "locationName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    final List<CsdlAnnotation> annotations = cut.getEdmItem().getAnnotations();
    assertEquals(1, annotations.size());
    assertEquals("Core.IsLanguageDependent", annotations.get(0).getTerm());
    assertEquals(ConstantExpressionType.Bool, annotations.get(0).getExpression().asConstant().getType());
    assertEquals("true", annotations.get(0).getExpression().asConstant().getValue());
    assertNull(annotations.get(0).getQualifier());
  }

  @Test
  void checkPostProcessorCalled() throws ODataJPAModelException {
    // PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.setPostProcessor(processor);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(cut, ADDR_CANONICAL_NAME);
  }

  @Test
  void checkPostProcessorAnnotationAdded() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);

    assertEquals(1L, cut.getEdmItem().getAnnotations().stream().filter(a -> a.getTerm().equals("Immutable")).count());
  }

  @Test
  void checkEmptyAssociationThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        false, false, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_ANNOTATION_MISSING.getKey(), act.getId());
  }

  @Test
  void checkUnknownAttributeAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, false, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.INVALID_DESCRIPTION_PROPERTY.getKey(), act.getId());
  }

  @Test
  void checkNoLocationAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, true, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING.getKey(), act.getId());
  }

  @Test
  void checkLocationAndLanguageAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, true, 2), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING.getKey(), act.getId());
  }

  @Test
  void checkGetPathReturns() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getPath());
  }

  @Test
  void checkPathLeftColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getLeftColumnsList().isEmpty());
  }

  @Test
  void checkPathRightColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getRightColumnsList().isEmpty());
  }

  @Test
  void checkGetLeafNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getLeaf());
  }

  @Test
  void checkPathGetPathEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getPath().isEmpty());
  }

  @Test
  void checkPathGetInverseLeftJoinColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getInverseLeftJoinColumnsList().isEmpty());
  }

  @Test
  void checkPathIsCollectionFalse() throws ODataJPAModelException {
    createDefaultCut();
    assertFalse(cut.getPath().isCollection());
  }

  @Test
  void checkPathGetPartnerNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getPartner());
  }

  @Test
  void checkPathGetJoinTableNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getJoinTable());
  }

  @Test
  void checkPathGetSource() throws ODataJPAModelException {
    createDefaultCut();
    final JPAStructuredType act = cut.getPath().getSourceType();
    assertEquals("BusinessPartner", act.getExternalName());
  }

  @Test
  void checkPathGetAliasNull() throws ODataJPAModelException {
    createDefaultCut();
    final JPAAssociationPath act = cut.getPath();
    assertNull(act.getAlias());
  }

  @Test
  void checkPathHasJoinTableNull() throws ODataJPAModelException {
    createDefaultCut();
    final JPAAssociationPath act = cut.getPath();
    assertNull(act.getJoinTable());
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADDR_CANONICAL_NAME)) {
        if (property.getInternalName().equals("countryName")) {
          final CsdlAnnotation annotation = new CsdlAnnotation();
          annotation.setTerm("Immutable");
          annotation.setExpression(new CsdlConstantExpression(ConstantExpressionType.Bool, "true"));
          property.addAnnotations(Collections.singletonList(annotation));
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}
  }

  private interface AnnotatedMember extends Member, AnnotatedElement {

  }

  private void createDefaultCut() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.lazyBuildEdmItem();
  }

  private Attribute<?, ?> createAttributeMock(final boolean association, final boolean associationName,
      final int langFields) {
    final Attribute<?, ?> attribute = mock(Attribute.class);
    final ManagedType<?> mgedType = mock(ManagedType.class);
    final Member member = mock(AnnotatedMember.class);

    when(attribute.getName()).thenReturn("WithLocationField");
    when(attribute.getJavaMember()).thenReturn(member);
    when(attribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return mgedType;
      }
    });
    when(attribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Country.class;
      }
    });

    when(mgedType.getJavaType()).thenAnswer(new Answer<Class<BusinessPartner>>() {
      @Override
      public Class<BusinessPartner> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    if (association) {
      final EdmDescriptionAssociation a = mock(EdmDescriptionAssociation.class);
      when(((AnnotatedElement) member).getAnnotation(EdmDescriptionAssociation.class)).thenReturn(a);
      if (associationName)
        when(a.descriptionAttribute()).thenReturn("name");
      if (langFields > 0)
        when(a.languageAttribute()).thenReturn("language");
      if (langFields > 1)
        when(a.localeAttribute()).thenReturn("location");
    }
    return attribute;
  }
}
