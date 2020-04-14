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
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Country;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestIntermediateDescriptionProperty extends TestMappingRoot {
  private TestHelper helper;
  private IntermediateDescriptionProperty cut;
  private JPAEdmMetadataPostProcessor processor;
  private IntermediateStructuredType et;
  private JPAEdmNameBuilder nameBuilder;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    EntityType<?> type = helper.getEntityType(BusinessPartner.class);
    et = new IntermediateEntityType(nameBuilder, type, helper.schema);
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
  }

  @Test
  public void checkProptertyCanBeCreated() throws ODataJPAModelException {
    EmbeddableType<?> emtype = helper.getEmbeddedableType("PostalAddressData");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(emtype, "countryName");
    assertNotNull(new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema));
  }

  @Test
  public void checkGetDescriptionAttributeReturnsAttribute() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getDescriptionAttribute());
    assertEquals("name", cut.getDescriptionAttribute().getInternalName());
  }

  @Test
  public void checkGetFixedValueAssignmentReturnsEmptyList() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getFixedValueAssignment());
    assertTrue(cut.getFixedValueAssignment().isEmpty());
  }

  @Test
  public void checkGetFixedValueAssignmentReturnsList() throws ODataJPAModelException {
    final EntityType<?> type = helper.getEntityType(Person.class);
    et = new IntermediateEntityType(nameBuilder, type, helper.schema);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "locationName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.lazyBuildEdmItem();
    assertNotNull(cut.getFixedValueAssignment());
    assertEquals(2, cut.getFixedValueAssignment().size());
  }

  @Test
  public void checkGetTypeReturnsDescAttributeType() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getType());
    assertEquals(String.class, cut.getType());
  }

  @Test
  public void checkIsAssociationeReturnsTrue() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.isAssociation());
  }

  @Test
  public void checkGetLocaleFieldNameReturnsPath() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getLocaleFieldName());
    assertEquals("\"LanguageISO\"", cut.getLocaleFieldName().getDBFieldName());
  }

  @Test
  public void checkGetProptertyNameOneToMany() throws ODataJPAModelException {
    createDefaultCut();
    assertEquals("CountryName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetProptertyNameManyToMany() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "regionName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals("RegionName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetProptertyType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        cut.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetTargetEntity() throws ODataJPAModelException {
    createDefaultCut();
    final JPAStructuredType target = cut.getTargetEntity();
    assertEquals("Country", target.getExternalName());
  }

  @Test
  public void checkGetPartnerNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPartner());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediatePropertyAccess property = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertTrue(cut.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyMaxLength() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    assertEquals(new Integer(100), cut.getEdmItem().getMaxLength());
  }

  @Test
  public void checkWrongPathElementThrowsEcxeption() {

    Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    EdmDescriptionAssoziation assoziation = prepareCheckPath(jpaAttribute);

    EdmDescriptionAssoziation.valueAssignment[] valueAssignments = new EdmDescriptionAssoziation.valueAssignment[1];
    EdmDescriptionAssoziation.valueAssignment valueAssignment = mock(EdmDescriptionAssoziation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationData/dummy");
    when(assoziation.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
      cut.getEdmItem();
    } catch (ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @Test
  public void checkWrongPathStartThrowsEcxeption() {

    Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    EdmDescriptionAssoziation assoziation = prepareCheckPath(jpaAttribute);

    EdmDescriptionAssoziation.valueAssignment[] valueAssignments = new EdmDescriptionAssoziation.valueAssignment[1];
    EdmDescriptionAssoziation.valueAssignment valueAssignment = mock(EdmDescriptionAssoziation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationDummy/dummy");
    when(assoziation.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
      cut.getEdmItem();
    } catch (ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private EdmDescriptionAssoziation prepareCheckPath(Attribute<?, ?> jpaAttribute) {
    AnnotatedMember jpaField = mock(AnnotatedMember.class);
    ManagedType jpaManagedType = mock(ManagedType.class);
    EdmDescriptionAssoziation assoziation = mock(EdmDescriptionAssoziation.class);

    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<BusinessPartner>>() {
      @Override
      public Class<BusinessPartner> answer(InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    when(jpaAttribute.getJavaMember()).thenReturn(jpaField);
    when(jpaAttribute.getName()).thenReturn("dummy");
    when(jpaAttribute.getDeclaringType()).thenReturn(jpaManagedType);
    when(jpaManagedType.getJavaType()).thenReturn(BusinessPartner.class);

    when(jpaField.getAnnotation(EdmDescriptionAssoziation.class)).thenReturn(assoziation);

    when(assoziation.descriptionAttribute()).thenReturn("country");
    when(assoziation.languageAttribute()).thenReturn("language");
    when(assoziation.localeAttribute()).thenReturn("");
    return assoziation;
  }

  @Test
  public void checkAnnotations() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "locationName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    List<CsdlAnnotation> annotations = cut.getEdmItem().getAnnotations();
    assertEquals(1, annotations.size());
    assertEquals("Core.IsLanguageDependent", annotations.get(0).getTerm());
    assertEquals(ConstantExpressionType.Bool, annotations.get(0).getExpression().asConstant().getType());
    assertEquals("true", annotations.get(0).getExpression().asConstant().getValue());
    assertNull(annotations.get(0).getQualifier());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    // PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.setPostProcessor(processor);
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(cut, ADDR_CANONICAL_NAME);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);

    assertEquals("CountryDescription", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    assertEquals("CountryDescription", property.getExternalName(), "Wrong name");
  }

  @Test
  public void checkEmptyAssoziationThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        false, false, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_ANNOTATION_MISSING.getKey(), act.getId());
  }

  @Test
  public void checkUnknownAttributeAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, false, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.INVALID_DESCIPTION_PROPERTY.getKey(), act.getId());
  }

  @Test
  public void checkNoLocationAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, true, 0), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING.getKey(), act.getId());
  }

  @Test
  public void checkLocationAndLanguageAtTargetThrowsException() throws ODataJPAModelException {

    final IntermediateDescriptionProperty cut = new IntermediateDescriptionProperty(nameBuilder, createAttributeMock(
        true, true, 2), et, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class, () -> cut.lazyBuildEdmItem());
    assertEquals(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING.getKey(), act.getId());
  }

  @Test
  public void checkGetPathReturns() throws ODataJPAModelException {
    createDefaultCut();
    assertNotNull(cut.getPath());
  }

  @Test
  public void checkPathLeftColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getLeftColumnsList().isEmpty());
  }

  @Test
  public void checkPathRightColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getRightColumnsList().isEmpty());
  }

  @Test
  public void checkGetLeafNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getLeaf());
  }

  @Test
  public void checkPathGetPathEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getPath().isEmpty());
  }

  @Test
  public void checkPathGetInverseLeftJoinColumnsListEmpty() throws ODataJPAModelException {
    createDefaultCut();
    assertTrue(cut.getPath().getInverseLeftJoinColumnsList().isEmpty());
  }

  @Test
  public void checkPathIsCollectionFalse() throws ODataJPAModelException {
    createDefaultCut();
    assertFalse(cut.getPath().isCollection());
  }

  @Test
  public void checkPathGetPartnerNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getPartner());
  }

  @Test
  public void checkPathGetJoinTableNull() throws ODataJPAModelException {
    createDefaultCut();
    assertNull(cut.getPath().getJoinTable());
  }

  @Test
  public void checkPathGetSource() throws ODataJPAModelException {
    createDefaultCut();
    final JPAStructuredType act = cut.getPath().getSourceType();
    assertEquals("BusinessPartner", act.getExternalName());
  }

  @Test
  public void checkPathGetAliasNull() throws ODataJPAModelException {
    createDefaultCut();
    final JPAAssociationPath act = cut.getPath();
    assertNull(act.getAlias());
  }

  @Test
  public void checkPathHasJoinTableNull() throws ODataJPAModelException {
    createDefaultCut();
    final JPAAssociationPath act = cut.getPath();
    assertNull(act.getJoinTable());
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADDR_CANONICAL_NAME)) {
        if (property.getInternalName().equals("countryName")) {
          property.setExternalName("CountryDescription");
        }
      }
    }

    @Override
    public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(IntermediateReferenceList references) throws ODataJPAModelException {}
  }

  private interface AnnotatedMember extends Member, AnnotatedElement {

  }

  private void createDefaultCut() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, et, helper.schema);
    cut.lazyBuildEdmItem();
  }

  private Attribute<?, ?> createAttributeMock(final boolean assozation, final boolean assozationName,
      final int langFields) {
    final Attribute<?, ?> attribute = mock(Attribute.class);
    final ManagedType<?> mgedType = mock(ManagedType.class);
    final Member member = mock(AnnotatedMember.class);

    when(attribute.getName()).thenReturn("WithLocationField");
    when(attribute.getJavaMember()).thenReturn(member);
    when(attribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(InvocationOnMock invocation) throws Throwable {
        return mgedType;
      }
    });
    when(attribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return Country.class;
      }
    });

    when(mgedType.getJavaType()).thenAnswer(new Answer<Class<BusinessPartner>>() {
      @Override
      public Class<BusinessPartner> answer(InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    if (assozation) {
      final EdmDescriptionAssoziation a = mock(EdmDescriptionAssoziation.class);
      when(((AnnotatedElement) member).getAnnotation(EdmDescriptionAssoziation.class)).thenReturn(a);
      if (assozationName)
        when(a.descriptionAttribute()).thenReturn("name");
      if (langFields > 0)
        when(a.languageAttribute()).thenReturn("language");
      if (langFields > 1)
        when(a.localeAttribute()).thenReturn("location");
    }
    return attribute;
  }
}
