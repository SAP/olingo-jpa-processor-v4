package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestIntermediateCollectionProperty extends TestMappingRoot {
  private IntermediateCollectionProperty cut;
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;

  private JPAEdmNameBuilder nameBuilder;
  private PluralAttribute<?, ?, ?> jpaAttribute;
  private ManagedType<?> managedType;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPAEdmNameBuilder(PUNIT_NAME);
    jpaAttribute = mock(PluralAttribute.class);
    managedType = mock(ManagedType.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void checkSimpleCollectionPropertyType() throws ODataJPAModelException {
    when(jpaAttribute.getName()).thenReturn("Text");
    @SuppressWarnings("rawtypes")
    javax.persistence.metamodel.Type type = mock(javax.persistence.metamodel.Type.class);
    when(type.getPersistenceType()).thenReturn(PersistenceType.BASIC);
    when(type.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
    when(jpaAttribute.getElementType()).thenReturn(type);
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(InvocationOnMock invocation) throws Throwable {
        return managedType;
      }
    });
    when(managedType.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return Person.class;
      }
    });
    when(jpaAttribute.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(InvocationOnMock invocation) throws Throwable {
        return List.class;
      }
    });
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, helper.schema);
    assertEquals("Edm.String", cut.getEdmItem().getType());
    assertEquals(String.class, cut.getType());
  }

  @Test
  public void checkGetProptertyComplexType() throws ODataJPAModelException {

    PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        "Person"), "inhouseAddress");
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals(PUNIT_NAME + ".InhouseAddress", property.getEdmItem().getType());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {

    PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        "Person"), "inhouseAddress");
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyDBFieldName() throws ODataJPAModelException {

    PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        "Organization"), "comment");
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    assertEquals("\"Text\"", property.getDBFieldName());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {

    IntermediateSimpleProperty.setPostProcessor(processor);
    PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        "Organization"), "comment");
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);
    property.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(property, ORG_CANONICAL_NAME);
  }

  @Test
  public void checkGetProptertyReturnsAnnotation() throws ODataJPAModelException {

    PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        "Person"), "inhouseAddress");
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute, helper.schema);

    List<CsdlAnnotation> annotations = property.getEdmItem().getAnnotations();
    assertEquals(1, property.getEdmItem().getAnnotations().size());
    assertTrue(annotations.get(0).getExpression().isConstant());
  }
}
