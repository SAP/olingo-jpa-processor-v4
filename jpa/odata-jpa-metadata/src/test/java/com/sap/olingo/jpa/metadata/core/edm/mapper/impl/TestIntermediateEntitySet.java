package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntitySetAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

public class TestIntermediateEntitySet extends TestMappingRoot {
  private IntermediateSchema schema;
  private Set<EntityType<?>> etList;
  private JPAEdmNameBuilder namebuilder;

  @Before
  public void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    etList = emf.getMetamodel().getEntities();
    namebuilder = new JPAEdmNameBuilder(PUNIT_NAME);
    schema = new IntermediateSchema(namebuilder, emf.getMetamodel());
  }

  @Test
  public void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessor());
    IntermediateEntityType et = new IntermediateEntityType(namebuilder, getEntityType(
        "AdministrativeDivisionDescription"), schema);
    IntermediateEntitySet set = new IntermediateEntitySet(namebuilder, et);
    List<CsdlAnnotation> act = set.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.TopSupported", act.get(0).getTerm());
  }

  @Test
  public void checkODataEntityTypeDiffers() throws ODataJPAModelException {
    IntermediateEntityType et = new IntermediateEntityType(new JPAEdmNameBuilder(PUNIT_NAME), getEntityType(
        "BestOrganization"), schema);
    IntermediateEntitySet set = new IntermediateEntitySet(namebuilder, et);

    JPAEntityType odataEt = set.getODataEntityType();
    assertEquals("BusinessPartner", odataEt.getExternalName());
  }

  @Test
  public void checkODataEntityTypeSame() throws ODataJPAModelException {
    IntermediateEntityType et = new IntermediateEntityType(new JPAEdmNameBuilder(PUNIT_NAME), getEntityType(
        "Organization"), schema);
    IntermediateEntitySet set = new IntermediateEntitySet(namebuilder, et);

    JPAEntityType odataEt = set.getODataEntityType();
    assertEquals("Organization", odataEt.getExternalName());
  }

  @Test
  public void checkEdmItemContainsODataEntityType() throws ODataJPAModelException {
    IntermediateEntityType et = new IntermediateEntityType(new JPAEdmNameBuilder(PUNIT_NAME), getEntityType(
        "BestOrganization"), schema);
    IntermediateEntitySet set = new IntermediateEntitySet(namebuilder, et);
    CsdlEntitySet act = set.getEdmItem();
    assertEquals(namebuilder.buildFQN("BusinessPartner").getFullQualifiedNameAsString(), act.getType());
  }

  private class PostProcessor extends JPAEdmMetadataPostProcessor {

    @Override
    public void processProperty(final IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(
          "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner")) {
        if (property.getInternalName().equals("communicationData")) {
          property.setIgnore(true);
        }
      }
    }

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}

    @Override
    public void processEntitySet(final IntermediateEntitySetAccess entitySet) {

      CsdlConstantExpression mimeType = new CsdlConstantExpression(ConstantExpressionType.Bool, "false");
      CsdlAnnotation annotation = new CsdlAnnotation();
      annotation.setExpression(mimeType);
      annotation.setTerm("Capabilities.TopSupported");
      List<CsdlAnnotation> annotations = new ArrayList<CsdlAnnotation>();
      annotations.add(annotation);
      entitySet.addAnnotations(annotations);
    }
  }

  private EntityType<?> getEntityType(final String typeName) {
    for (EntityType<?> entityType : etList) {
      if (entityType.getJavaType().getSimpleName().equals(typeName)) {
        return entityType;
      }
    }
    return null;
  }
}
