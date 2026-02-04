package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.processor.core.util.Assertions.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.persistence.Entity;
import jakarta.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.TermAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntitySetAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.util.AnnotationTestHelper;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictionsProperties;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictionsProperties;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictionsProperties;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.Terms;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateMethod;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateRestrictionsProperties;
import com.sap.olingo.jpa.metadata.odata.v4.general.Aliases;
import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCapabilitiesAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsParent;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class IntermediateEntitySetTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private Set<EntityType<?>> etList;
  private JPADefaultEdmNameBuilder nameBuilder;
  private IntermediateAnnotationInformation annotationInfo;
  private IntermediateReferences references;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final var reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    etList = emf.getMetamodel().getEntities();
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    references = mock(IntermediateReferences.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>(), references);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), reflections, annotationInfo, true);
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    final var et = new IntermediateEntityType<>(nameBuilder,
        getEntityType("AdministrativeDivisionDescription"), schema);
    final var es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.TopSupported", act.get(0).getTerm());
  }

  @Test
  void checkODataEntityTypeDiffers() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final var es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final var odataEt = es.getODataEntityType();
    assertEquals("BusinessPartner", odataEt.getExternalName());
  }

  @Test
  void checkODataEntityTypeSame() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);

    final var es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final var odataEt = es.getODataEntityType();
    assertEquals("Organization", odataEt.getExternalName());
  }

  @Test
  void checkEdmItemContainsODataEntityType() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final var es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.getEdmItem();
    assertEquals(et.buildFQN("BusinessPartner").getFullQualifiedNameAsString(), act.getType());
  }

  @Test
  void checkConvertStringToPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.convertStringToPath("type");
    assertNotNull(act);
    assertEquals("Type", act.getPathAsString());
  }

  @Test
  void checkConvertStringToNavigationPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.convertStringToNavigationPath("roles");
    assertNotNull(act);
    assertEquals("Roles", act.getPathAsString());
  }

  @Test
  void checkJavaAnnotationsOneAnnotation() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.javaAnnotations(EdmEntityType.class.getPackage().getName());
    assertEquals(2, act.size());
    assertNotNull(act.get("EdmEntityType"));
    assertNotNull(act.get("EdmFunctions"));
  }

  @Test
  void checkJavaAnnotationsTwoAnnotations() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.javaAnnotations(Entity.class.getPackage().getName());
    assertEquals(4, act.size());
    assertNotNull(act.get("Table"));
    assertNotNull(act.get("Entity"));
    assertNotNull(act.get("Inheritance"));
    assertNotNull(act.get("DiscriminatorColumn"));
  }

  @Test
  void checkJavaAnnotationsNoAnnotations() throws ODataJPAModelException {
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.javaAnnotations(Test.class.getPackage().toString());
    assertTrue(act.isEmpty());
  }

  @Test
  void checkGetAnnotationReturnsExistingAnnotation() throws ODataJPAModelException {
    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final var act = es.getAnnotation("Capabilities", "FilterRestrictions");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotation("Capability", "FilterRestrictions"));
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotation("Capabilities", "Filter"));
  }

  @Test
  void checkGetAnnotationValueReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotationValue("Capabilities", "Filter", "Filterable"));
  }

  static Stream<Arguments> expectedSimpleValue() {
    return Stream.of(
        Arguments.of(Terms.FILTER_RESTRICTIONS, FilterRestrictionsProperties.FILTERABLE, Boolean.class, Boolean.TRUE),
        Arguments.of(Terms.UPDATE_RESTRICTIONS, UpdateRestrictionsProperties.UPDATE_METHOD, String.class,
            UpdateMethod.PATCH.name()),
        Arguments.of(Terms.UPDATE_RESTRICTIONS, UpdateRestrictionsProperties.DESCRIPTION, String.class, "Just to test"),
        Arguments.of(Terms.EXPAND_RESTRICTIONS, ExpandRestrictionsProperties.MAX_LEVELS, Integer.class, Integer.valueOf(
            2)));
  }

  @ParameterizedTest
  @MethodSource("expectedSimpleValue")
  void checkGetAnnotationValueSimpleProperty(final TermAccess term, final PropertyAccess propertyName,
      final Class<?> type, final Object expectedValue) throws ODataJPAModelException {

    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final var act = es.getAnnotationValue("Capabilities", term.term(), propertyName.property());
    assertNotNull(act);
    assertTrue(type.isAssignableFrom(act.getClass()));
    assertEquals(expectedValue, act);

    assertEquals(expectedValue, es.getAnnotationValue("Capabilities", term.term(), propertyName.property(), type));
    assertEquals(expectedValue, es.getAnnotationValue(Aliases.CAPABILITIES, term, propertyName, type));
  }

  static Stream<Arguments> expectedCollectionValue() {
    return Stream.of(
        Arguments.of(Terms.FILTER_RESTRICTIONS, FilterRestrictionsProperties.REQUIRED_PROPERTIES, Arrays.asList(
            "ParentCodeID", "ParentDivisionCode"), false),
        Arguments.of(Terms.COUNT_RESTRICTIONS, CountRestrictionsProperties.NON_COUNTABLE_NAVIGATION_PROPERTIES, Arrays
            .asList("Children"), true));
  }

  @ParameterizedTest
  @MethodSource("expectedCollectionValue")
  void checkGetAnnotationValueCollectionProperty(final TermAccess term, final PropertyAccess propertyName,
      final List<String> expectedValues, final boolean isNavigation) throws ODataJPAModelException {

    createAnnotations();
    final var et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final List<?> exp;
    if (isNavigation) {
      exp = expectedValues.stream().map(name -> {
        try {
          return et.getAssociationPath(name);
        } catch (final ODataJPAModelException e) {
          fail(e);
          return null;
        }
      }).toList();
    } else {
      exp = expectedValues.stream().map(name -> {
        try {
          return et.getPath(name);
        } catch (final ODataJPAModelException e) {
          fail(e);
          return null;
        }
      }).toList();
    }
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final var act = es.getAnnotationValue("Capabilities", term.term(), propertyName.property());
    assertNotNull(act);
    assertEquals(exp, act);
  }

  @Test
  void checkAsUserGroupRestrictedUserRestrictsNavigations() throws ODataJPAModelException {
    final IntermediateEntityType<Person> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), getEntityType(Person.class), schema);
    final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final IntermediateEntitySet act = es.asUserGroupRestricted(List.of("Company"), true);
    assertEquals(es.getExternalFQN(), act.getExternalFQN());
    assertEquals(es.getExternalName(), act.getExternalName());
    assertEquals(es.getInternalName(), act.getInternalName());
    assertListEquals(es.getEdmItem().getNavigationPropertyBindings(), act.getEdmItem().getNavigationPropertyBindings(),
        CsdlNavigationPropertyBinding.class);
    final IntermediateEntitySet act2 = es.asUserGroupRestricted(List.of("Person"), true);
    assertEquals(es.getExternalFQN(), act2.getExternalFQN());
    assertEquals(es.getExternalName(), act2.getExternalName());
    assertEquals(es.getInternalName(), act2.getInternalName());
    assertEquals(es.getEdmItem().getNavigationPropertyBindings().size() - 1, act2.getEdmItem()
        .getNavigationPropertyBindings().size());
  }

  private void createAnnotations() {
    final AnnotationProvider annotationProvider = new JavaBasedCapabilitiesAnnotationsProvider();
    final List<CsdlProperty> propertiesFilter = new ArrayList<>();
    final List<CsdlProperty> propertiesExpand = new ArrayList<>();
    final List<CsdlProperty> propertiesUpdate = new ArrayList<>();
    final List<CsdlProperty> propertiesCount = new ArrayList<>();

    final var termFilter = AnnotationTestHelper.addTermToCapabilitiesReferences(references, "FilterRestrictions",
        "FilterRestrictionsType", propertiesFilter);
    final var termExpand = AnnotationTestHelper.addTermToCapabilitiesReferences(references, "ExpandRestrictions",
        "ExpandRestrictionsType", propertiesExpand);
    final var termUpdate = AnnotationTestHelper.addTermToCapabilitiesReferences(references, "UpdateRestrictions",
        "UpdateRestrictionsType", propertiesUpdate);
    final var termCount = AnnotationTestHelper.addTermToCapabilitiesReferences(references, "CountRestrictions",
        "CountRestrictionsType", propertiesCount);

    propertiesFilter.add(AnnotationTestHelper.createTermProperty("Filterable", "Edm.Boolean"));
    propertiesFilter.add(AnnotationTestHelper.createTermCollectionProperty("RequiredProperties", "Edm.PropertyPath"));
    propertiesExpand.add(AnnotationTestHelper.createTermProperty("MaxLevels", "Edm.Int32"));
    propertiesUpdate.add(AnnotationTestHelper.createTermProperty("UpdateMethod", "Capabilities.HttpMethod"));
    propertiesUpdate.add(AnnotationTestHelper.createTermProperty("Description", "Edm.String"));
    propertiesCount.add(AnnotationTestHelper.createTermCollectionProperty("NonCountableNavigationProperties",
        "Edm.NavigationPropertyPath"));

    when(references.convertAlias("Capabilities")).thenReturn("Org.OData.Capabilities.V1");
    when(references.getTerms("Capabilities", Applicability.ENTITY_SET))
        .thenReturn(Arrays.asList(termFilter, termExpand, termUpdate, termCount));
    annotationInfo.getAnnotationProvider().add(annotationProvider);
  }

  private static class PostProcessor implements JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals("com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")
          && property.getInternalName().equals("communicationData")) {
        property.setIgnore(true);
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      // Not needed
    }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {
      // Not needed
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      // Not needed
    }

    @Override
    public void processEntitySet(final IntermediateEntitySetAccess entitySet) {

      final var mimeType = new CsdlConstantExpression(ConstantExpressionType.Bool, "false");
      final var annotation = new CsdlAnnotation();
      annotation.setExpression(mimeType);
      annotation.setTerm("Capabilities.TopSupported");
      final List<CsdlAnnotation> annotations = new ArrayList<>();
      annotations.add(annotation);
      entitySet.addAnnotations(annotations);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> EntityType<T> getEntityType(final String typeName) {
    for (final EntityType<?> entityType : etList) {
      if (entityType.getJavaType().getSimpleName().equals(typeName)) {
        return (EntityType<T>) entityType;
      }
    }
    return null;
  }
}
