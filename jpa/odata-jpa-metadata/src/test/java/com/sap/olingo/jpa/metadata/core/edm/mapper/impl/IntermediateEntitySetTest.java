package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPathNotFoundException;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataPropertyPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntitySetAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AnnotationsParent;
import com.sap.olingo.jpa.processor.core.testmodel.BestOrganization;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class IntermediateEntitySetTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private Set<EntityType<?>> etList;
  private JPADefaultEdmNameBuilder nameBuilder;
  private IntermediateAnnotationInformation annotationInfo;
  private IntermediateReferences references;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections reflections = mock(Reflections.class);
    when(reflections.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    etList = emf.getMetamodel().getEntities();
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    references = mock(IntermediateReferences.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>(), references);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), reflections, annotationInfo);
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(nameBuilder,
        getEntityType("AdministrativeDivisionDescription"), schema);
    final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final List<CsdlAnnotation> act = es.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.TopSupported", act.get(0).getTerm());
  }

  @Test
  void checkODataEntityTypeDiffers() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final JPAEntityType odataEt = es.getODataEntityType();
    assertEquals("BusinessPartner", odataEt.getExternalName());
  }

  @Test
  void checkODataEntityTypeSame() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);

    final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);

    final JPAEntityType odataEt = es.getODataEntityType();
    assertEquals("Organization", odataEt.getExternalName());
  }

  @Test
  void checkEdmItemContainsODataEntityType() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final IntermediateEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final CsdlEntitySet act = es.getEdmItem();
    assertEquals(et.buildFQN("BusinessPartner").getFullQualifiedNameAsString(), act.getType());
  }

  @Test
  void checkConvertStringToPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final ODataPropertyPath act = es.convertStringToPath("type");
    assertNotNull(act);
    assertEquals("Type", act.getPathAsString());
  }

  @Test
  void checkConvertStringToNavigationPathWithSimplePath() throws ODataJPAModelException, ODataPathNotFoundException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final ODataNavigationPath act = es.convertStringToNavigationPath("roles");
    assertNotNull(act);
    assertEquals("Roles", act.getPathAsString());
  }

  @Test
  void checkJavaAnnotationsOneAnnotation() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final Map<String, Annotation> act = es.javaAnnotations(EdmEntityType.class.getPackage().getName());
    assertEquals(2, act.size());
    assertNotNull(act.get("EdmEntityType"));
    assertNotNull(act.get("EdmFunctions"));
  }

  @Test
  void checkJavaAnnotationsTwoAnnotations() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final Map<String, Annotation> act = es.javaAnnotations(Entity.class.getPackage().getName());
    assertEquals(4, act.size());
    assertNotNull(act.get("Table"));
    assertNotNull(act.get("Entity"));
    assertNotNull(act.get("Inheritance"));
    assertNotNull(act.get("DiscriminatorColumn"));
  }

  @Test
  void checkJavaAnnotationsNoAnnotations() throws ODataJPAModelException {
    final IntermediateEntityType<BusinessPartner> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(BusinessPartner.class), schema);
    final IntermediateTopLevelEntity es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final Map<String, Annotation> act = es.javaAnnotations(Test.class.getPackage().toString());
    assertTrue(act.isEmpty());
  }

  @Test
  void checkGetAnnotationReturnsExistingAnnotation() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    final CsdlAnnotation act = es.getAnnotation("Capabilities", "FilterRestrictions");
    assertNotNull(act);
  }

  @Test
  void checkGetAnnotationReturnsNullAliasUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotation("Capability", "FilterRestrictions"));
  }

  @Test
  void checkGetAnnotationReturnsNullAnnotationUnknown() throws ODataJPAModelException {
    createAnnotation();
    final IntermediateEntityType<AnnotationsParent> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType(AnnotationsParent.class), schema);
    final JPAEntitySet es = new IntermediateEntitySet(nameBuilder, et, annotationInfo);
    assertNull(es.getAnnotation("Capabilities", "Filter"));
  }

  private void createAnnotation() {
    final AnnotationProvider annotationProvider = mock(AnnotationProvider.class);
    final List<CsdlAnnotation> annotations = new ArrayList<>();
    final CsdlAnnotation annotation = mock(CsdlAnnotation.class);
    annotations.add(annotation);
    when(references.convertAlias("Capabilities")).thenReturn("Org.OData.Capabilities.V1");
    when(annotation.getTerm()).thenReturn("Org.OData.Capabilities.V1.FilterRestrictions");
    annotationInfo.getAnnotationProvider().add(annotationProvider);
    when(annotationProvider.getAnnotations(eq(Applicability.ENTITY_SET), any(), any()))
        .thenReturn(annotations);
  }

  private class PostProcessor extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("communicationData")) {
          property.setIgnore(true);
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

    @Override
    public void processEntitySet(final IntermediateEntitySetAccess entitySet) {

      final CsdlConstantExpression mimeType = new CsdlConstantExpression(ConstantExpressionType.Bool, "false");
      final CsdlAnnotation annotation = new CsdlAnnotation();
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
