package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntitySetAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BestOrganization;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class IntermediateEntitySetTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private Set<EntityType<?>> etList;
  private JPADefaultEdmNameBuilder nameBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections r = mock(Reflections.class);
    when(r.getTypesAnnotatedWith(EdmEnumeration.class)).thenReturn(new HashSet<>(Arrays.asList(
        ABCClassification.class)));

    etList = emf.getMetamodel().getEntities();
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    schema = new IntermediateSchema(nameBuilder, emf.getMetamodel(), r);
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    final IntermediateEntityType<AdministrativeDivisionDescription> et = new IntermediateEntityType<>(nameBuilder,
        getEntityType("AdministrativeDivisionDescription"), schema);
    final IntermediateEntitySet set = new IntermediateEntitySet(nameBuilder, et);
    final List<CsdlAnnotation> act = set.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.TopSupported", act.get(0).getTerm());
  }

  @Test
  void checkODataEntityTypeDiffers() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final IntermediateEntitySet set = new IntermediateEntitySet(nameBuilder, et);

    final JPAEntityType odataEt = set.getODataEntityType();
    assertEquals("BusinessPartner", odataEt.getExternalName());
  }

  @Test
  void checkODataEntityTypeSame() throws ODataJPAModelException {
    final IntermediateEntityType<Organization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("Organization"), schema);

    final IntermediateEntitySet set = new IntermediateEntitySet(nameBuilder, et);

    final JPAEntityType odataEt = set.getODataEntityType();
    assertEquals("Organization", odataEt.getExternalName());
  }

  @Test
  void checkEdmItemContainsODataEntityType() throws ODataJPAModelException {
    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final IntermediateEntitySet set = new IntermediateEntitySet(nameBuilder, et);
    final CsdlEntitySet act = set.getEdmItem();
    assertEquals(et.buildFQN("BusinessPartner").getFullQualifiedNameAsString(), act.getType());
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
