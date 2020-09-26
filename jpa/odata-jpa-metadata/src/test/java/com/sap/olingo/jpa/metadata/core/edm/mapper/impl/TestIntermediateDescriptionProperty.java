package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;

public class TestIntermediateDescriptionProperty extends TestMappingRoot {
  private TestHelper helper;
  private IntermediateDescriptionProperty cut;
  private JPAEdmMetadataPostProcessor processor;
  private JPAEdmNameBuilder nameBuilder;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
  }

  @Test
  public void checkProptertyCanBeCreated() throws ODataJPAModelException {
    final EmbeddableType<?> emtype = helper.getEmbeddedableType("PostalAddressData");
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(emtype, "countryName");
    assertNotNull(new IntermediateDescriptionProperty(nameBuilder, jpaAttribute, helper.schema));
  }

  @Test
  public void checkGetProptertyNameOneToMany() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("CountryName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetProptertyNameManyToMany() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "regionName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("RegionName", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkGetProptertyType() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        cut.getEdmItem().getType(), "Wrong type");
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    final IntermediatePropertyAccess property = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(cut.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyMaxLength() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(100), cut.getEdmItem().getMaxLength());
  }

  @Test
  public void checkWrongPathElementThrowsEcxeption() {

    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final EdmDescriptionAssoziation assoziation = prepareCheckPath(jpaAttribute);

    final EdmDescriptionAssoziation.valueAssignment[] valueAssignments =
        new EdmDescriptionAssoziation.valueAssignment[1];
    final EdmDescriptionAssoziation.valueAssignment valueAssignment = mock(
        EdmDescriptionAssoziation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationData/dummy");
    when(assoziation.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
          helper.schema);
      cut.getEdmItem();
    } catch (final ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @Test
  public void checkWrongPathStartThrowsEcxeption() {

    final Attribute<?, ?> jpaAttribute = mock(Attribute.class);
    final EdmDescriptionAssoziation assoziation = prepareCheckPath(jpaAttribute);

    final EdmDescriptionAssoziation.valueAssignment[] valueAssignments =
        new EdmDescriptionAssoziation.valueAssignment[1];
    final EdmDescriptionAssoziation.valueAssignment valueAssignment = mock(
        EdmDescriptionAssoziation.valueAssignment.class);
    valueAssignments[0] = valueAssignment;
    when(valueAssignment.attribute()).thenReturn("communicationDummy/dummy");
    when(assoziation.valueAssignments()).thenReturn(valueAssignments);

    try {
      cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
          helper.schema);
      cut.getEdmItem();
    } catch (final ODataJPAModelException e) {
      return;
    }
    fail();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private EdmDescriptionAssoziation prepareCheckPath(final Attribute<?, ?> jpaAttribute) {
    final AnnotatedMember jpaField = mock(AnnotatedMember.class);
    final ManagedType jpaManagedType = mock(ManagedType.class);
    final EdmDescriptionAssoziation assoziation = mock(EdmDescriptionAssoziation.class);

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

    when(jpaField.getAnnotation(EdmDescriptionAssoziation.class)).thenReturn(assoziation);

    when(assoziation.descriptionAttribute()).thenReturn("country");
    when(assoziation.languageAttribute()).thenReturn("language");
    when(assoziation.localeAttribute()).thenReturn("");
    return assoziation;
  }

  @Test
  public void checkAnnotations() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(BusinessPartner.class),
        "locationName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    final List<CsdlAnnotation> annotations = cut.getEdmItem().getAnnotations();
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
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    cut.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(cut, ADDR_CANONICAL_NAME);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertEquals("CountryDescription", cut.getEdmItem().getName(), "Wrong name");
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    final PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.setPostProcessor(pPDouble);

    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    final IntermediatePropertyAccess property = new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);

    assertEquals("CountryDescription", property.getExternalName(), "Wrong name");
  }

  private class PostProcessorSetName extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(ADDR_CANONICAL_NAME)) {
        if (property.getInternalName().equals("countryName")) {
          property.setExternalName("CountryDescription");
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
}
