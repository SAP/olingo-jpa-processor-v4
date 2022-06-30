package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionSecondLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.ComplexWithTransientComplexCollection;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class IntermediateCollectionPropertyTest extends TestMappingRoot {
  private IntermediateCollectionProperty<?> cut;
  private TestHelper helper;
  private JPAEdmMetadataPostProcessor processor;

  private JPADefaultEdmNameBuilder nameBuilder;
  private PluralAttribute<?, ?, ?> jpaAttribute;
  private ManagedType<?> managedType;
  private IntermediateSchema schema;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    processor = mock(JPAEdmMetadataPostProcessor.class);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    jpaAttribute = mock(PluralAttribute.class);
    managedType = mock(ManagedType.class);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), mock(Reflections.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void checkSimpleCollectionPropertyType() throws ODataJPAModelException {
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
    cut = new IntermediateCollectionProperty<>(nameBuilder, jpaAttribute, helper.schema, helper.schema.getEntityType(
        Organization.class));
    assertEquals("Edm.String", cut.getEdmItem().getType());
    assertEquals(String.class, cut.getType());
  }

  @Test
  void checkGetPropertyComplexType() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));
    assertEquals(PUNIT_NAME + ".InhouseAddress", property.getEdmItem().getType());
  }

  @Test
  void checkGetPropertyIgnoreFalse() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));
    assertFalse(property.ignore());
  }

  @Test
  void checkGetPropertyDBFieldName() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Organization.class), "comment");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Organization.class));
    assertEquals("\"Text\"", property.getDBFieldName());
  }

  @Test
  void checkPostProcessorCalled() throws ODataJPAModelException {

    IntermediateSimpleProperty.setPostProcessor(processor);
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Organization.class), "comment");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Organization.class));
    property.getEdmItem();
    verify(processor, atLeastOnce()).processProperty(property, ORG_CANONICAL_NAME);
  }

  @Test
  void checkGetPropertyReturnsAnnotation() throws ODataJPAModelException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    final List<CsdlAnnotation> annotations = property.getEdmItem().getAnnotations();
    assertEquals(1, property.getEdmItem().getAnnotations().size());
    assertTrue(annotations.get(0).getExpression().isConstant());
  }

  @Test
  void checkGetDeepComplexPropertyReturnsExternalName() throws ODataJPAModelException {

    final IntermediateStructuredType<CollectionSecondLevelComplex> st = new IntermediateComplexType<>(nameBuilder,
        helper.getComplexType("CollectionSecondLevelComplex"), helper.schema);
    for (final JPACollectionAttribute collection : st.getDeclaredCollectionAttributes()) {
      if (collection.getInternalName().equals("comment")) {
        assertEquals("Comment", collection.asAssociation().getAlias());
        assertEquals(collection.asAssociation(), ((JPAAssociationAttribute) collection).getPath());
      }
    }
    final IntermediateStructuredType<CollectionFirstLevelComplex> stst = new IntermediateComplexType<>(nameBuilder,
        helper.getComplexType("CollectionFirstLevelComplex"), helper.schema);
    for (final JPAPath collection : stst.getCollectionAttributesPath()) {
      if (collection.getLeaf().getInternalName().equals("comment")) {
        assertEquals("SecondLevel/Comment", collection.getAlias());
      }
    }
  }

  @Test
  void checkAnnotations() throws ODataJPAModelException {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> cut = new IntermediateCollectionProperty<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    final List<CsdlAnnotation> annotations = cut.getEdmItem().getAnnotations();
    assertEquals(1, annotations.size());
    assertEquals("Core.Description", annotations.get(0).getTerm());
    assertEquals(ConstantExpressionType.String, annotations.get(0).getExpression().asConstant().getType());
    assertEquals("Address for inhouse Mail", annotations.get(0).getExpression().asConstant().getValue());
    assertEquals("Address", annotations.get(0).getQualifier());
  }

  @Test
  void checkIsTransientOfPrimitiveReturnsTrue() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = new IntermediateStructuredType.TransientPluralAttribute<>(
        helper.getEmbeddableType(CollectionFirstLevelComplex.class),
        CollectionFirstLevelComplex.class.getDeclaredField("transientCollection"),
        schema);

    final IntermediateCollectionProperty<?> cut = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getStructuredType(CollectionFirstLevelComplex.class));
    assertTrue(cut.isTransient());
    assertEquals("Edm.String", cut.getEdmItem().getType());
  }

  @Test
  void checkIsTransientOfComplexReturnsTrue() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    final IntermediateCollectionProperty<?> cut = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getStructuredType(ComplexWithTransientComplexCollection.class));
    assertTrue(cut.isTransient());
    assertEquals("com.sap.olingo.jpa.InhouseAddress", cut.getEdmItem().getType());
  }

  @Test
  void checkTransientCollectionType() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    assertEquals(CollectionType.LIST, jpaAttribute.getCollectionType());
  }

  @Test
  void checkTransientBindableTypeNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    assertNull(jpaAttribute.getBindableType());
  }

  @Test
  void checkTransientBindableJavaTypeNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    assertNull(jpaAttribute.getBindableJavaType());
  }

  @Test
  void checkTransientPersistentAttributeType() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    assertEquals(PersistentAttributeType.ELEMENT_COLLECTION, jpaAttribute.getPersistentAttributeType());
  }

  @Test
  void checkTransientIsCollectionTrue() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = createTransientPluralAttribute();
    assertTrue(jpaAttribute.isCollection());
  }

  @Test
  void checkGetPartnerReturnsNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertNull(property.getPartner());
  }

  @Test
  void checkIsAssociationFalse() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertFalse(property.isAssociation());
  }

  @Test
  void checkIsSearchableFalse() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertFalse(property.isSearchable());
  }

  @Test
  void checkIsEtagFalse() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertFalse(property.isEtag());
  }

  @Test
  void checkGetTargetEntity() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertNotNull(property.getTargetEntity());
  }

  @Test
  void checkGetTargetAttributeOfSimpleNotNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Organization.class), "comment");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Organization.class));

    final JPAAttribute act = property.getTargetAttribute();

    assertNotNull(act);
    assertEquals(EdmPrimitiveType.EDM_NAMESPACE + "." + EdmPrimitiveTypeKind.String,
        ((CsdlProperty) act.getProperty()).getType());
  }

  @Test
  void checkGetTargetAttributeOfComplexNull() throws ODataJPAModelException, NoSuchFieldException,
      SecurityException {

    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        Person.class), "inhouseAddress");
    final IntermediateCollectionProperty<?> property = new IntermediateCollectionProperty<>(nameBuilder,
        jpaAttribute, helper.schema, helper.schema.getEntityType(Person.class));

    assertNull(property.getTargetAttribute());
  }

  private PluralAttribute<?, ?, ?> createTransientPluralAttribute() throws NoSuchFieldException {
    final PluralAttribute<?, ?, ?> jpaAttribute = new IntermediateStructuredType.TransientPluralAttribute<>(
        helper.getEmbeddableType(ComplexWithTransientComplexCollection.class),
        ComplexWithTransientComplexCollection.class.getDeclaredField("transientCollection"),
        schema);
    return jpaAttribute;
  }
}
