package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionSecondLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestIntermediateCollectionProperty extends TestMappingRoot {
  private IntermediateCollectionProperty cut;
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;

  private JPADefaultEdmNameBuilder nameBuilder;
  private PluralAttribute<?, ?, ?> jpaAttribute;
  private ManagedType<?> managedType;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    jpaAttribute = mock(PluralAttribute.class);
    managedType = mock(ManagedType.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void checkSimpleCollectionPropertyType() throws ODataJPAModelException {
    when(jpaAttribute.getName()).thenReturn("Text");
    @SuppressWarnings("rawtypes")
    final javax.persistence.metamodel.Type type = mock(javax.persistence.metamodel.Type.class);
    when(type.getPersistenceType()).thenReturn(PersistenceType.BASIC);
    when(type.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
    when(jpaAttribute.getElementType()).thenReturn(type);
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return managedType;
      }
    });
    when(managedType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return Person.class;
      }
    });
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return List.class;
      }
    });
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, helper.schema, helper.schema.getEntityType(
        Organization.class));
    assertEquals("Edm.String", cut.getEdmItem().getType());
    assertEquals(String.class, cut.getType());
  }

  @Test
  public void checkGetProptertyComplexType() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));
    assertEquals(PUNIT_NAME + ".InhouseAddress", property.getEdmItem().getType());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyDBFieldName() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Organization.class), "comment");
    final IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Organization.class));
    assertEquals("\"Text\"", property.getDBFieldName());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {

    IntermediateSimpleProperty.setPostProcessor(processor);
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Organization.class), "comment");
    final IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Organization.class));
    property.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(property, ORG_CANONICAL_NAME);
  }

  @Test
  public void checkGetPropertyReturnsAnnotation() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    final List<CsdlAnnotation> annotations = property.getEdmItem().getAnnotations();
    assertEquals(1, property.getEdmItem().getAnnotations().size());
    assertTrue(annotations.get(0).getExpression().isConstant());
  }

  @Test
  public void checkGetDeepComplexPropertyReturnsExternalName() throws ODataJPAModelException {

    final IntermediateStructuredType<CollectionSecondLevelComplex> st = new IntermediateComplexType<>(nameBuilder,
        helper.getComplexType("CollectionSecondLevelComplex"), helper.schema);
    for (final JPACollectionAttribute collection : st.getDeclaredCollectionAttributes())
      if (collection.getInternalName().equals("comment")) assertEquals("Comment", collection.asAssociation()
          .getAlias());

    final IntermediateStructuredType<CollectionFirstLevelComplex> stst = new IntermediateComplexType<>(nameBuilder,
        helper.getComplexType("CollectionFirstLevelComplex"), helper.schema);
    for (final JPAPath collection : stst.getCollectionAttributesPath())
      if (collection.getLeaf().getInternalName().equals("comment")) assertEquals("SecondLevel/Comment", collection
          .getAlias());
  }

  @Test
  public void checkAnnotations() throws ODataJPAModelException {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty cut = new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    final List<CsdlAnnotation> annotations = cut.getEdmItem().getAnnotations();
    assertEquals(1, annotations.size());
    assertEquals("Core.Description", annotations.get(0).getTerm());
    assertEquals(ConstantExpressionType.String, annotations.get(0).getExpression().asConstant().getType());
    assertEquals("Address for inhouse Mail", annotations.get(0).getExpression().asConstant().getValue());
    assertEquals("Address", annotations.get(0).getQualifier());
  }
}
