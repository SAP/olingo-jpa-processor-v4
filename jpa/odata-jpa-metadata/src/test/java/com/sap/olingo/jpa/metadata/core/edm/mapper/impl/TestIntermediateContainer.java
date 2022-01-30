package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ConfigurationBuilder;
import org.reflections8.util.FilterBuilder;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityContainerAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.testmodel.BestOrganization;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;

class TestIntermediateContainer extends TestMappingRoot {
  private static final String PACKAGE1 = "com.sap.olingo.jpa.metadata.core.edm.mapper.impl";
  private static final String PACKAGE2 = "com.sap.olingo.jpa.processor.core.testmodel";
  private final HashMap<String, IntermediateSchema> schemas = new HashMap<>();
  private Set<EntityType<?>> etList;
  private IntermediateSchema schema;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    final Reflections r =
        new Reflections(
            new ConfigurationBuilder()
                .forPackages(PACKAGE1, PACKAGE2)
                .filterInputsBy(new FilterBuilder().includePackage(PACKAGE1, PACKAGE2))
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner()));

    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), r);
    etList = emf.getMetamodel().getEntities();
    schemas.put(PUNIT_NAME, schema);
  }

  @Test
  void checkContainerCanBeCreated() throws ODataJPAModelException {
    assertNotNull(new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(PUNIT_NAME), schemas));
  }

  @Test
  void checkGetName() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    assertEquals("ComSapOlingoJpaContainer", container.getExternalName());
  }

  @Test
  void checkGetNoEntitySets() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    assertEquals(TestDataConstants.NO_ENTITY_SETS, container.getEdmItem().getEntitySets().size());
  }

  @Test
  void checkGetNoSingletons() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    assertEquals(TestDataConstants.NO_SINGLETONS, container.getEdmItem().getSingletons().size());
  }

  @Test
  void checkGetEntitySetName() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) return;
    }
    fail();
  }

  @Test
  void checkGetEntitySetType() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        assertEquals(container.buildFQN("BusinessPartner"), entitySet.getTypeFQN());
        return;
      }
    }
    fail();
  }

  @Test
  void checkGetNoNavigationPropertyBindings() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        assertEquals(4, entitySet.getNavigationPropertyBindings().size());
        return;
      }
    }
    fail();
  }

  @Test
  void checkGetNavigationPropertyBindingsPath() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (final CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("Roles".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  void checkGetNavigationPropertyBindingsTarget() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (final CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("Roles".equals(binding.getPath())) {
            assertEquals("BusinessPartnerRoles", binding.getTarget());
            return;
          }
        }
      }
    }
    fail();
  }

  @Test
  void checkGetNavigationPropertyBindingsPathComplexType() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (final CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("Address/AdministrativeDivision".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  void checkGetNavigationPropertyBindingsPathComplexTypeNested() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schemas);

    final List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (final CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (final CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("AdministrativeInformation/Created/User".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  void checkGetNoFunctionImportIfBound() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (final CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("CountRoles")) {
        fail("Bound function must not generate a function import");
      }
    }
  }

  @Test
  void checkGetNoFunctionImportIfUnBoundHasImportFalse() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (final CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("max")) {
        fail("UnBound function must not generate a function import is not annotated");
      }
    }
  }

  @Test
  void checkGetNoFunctionImportForJavaBasedFunction() throws ODataJPAModelException {
    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (final CsdlFunctionImport funcImport : funcImports) {
      if ("Sum".equals(funcImport.getName()))
        return;
      System.out.println(funcImport.getName());
    }
    fail("Import not found");
  }

  @Test
  void checkGetFunctionImportIfUnBoundHasImportTrue() throws ODataJPAModelException {

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);

    final List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (final CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("Olingo V4 ")) {
        fail("UnBound function must be generate a function import is annotated");
      }
    }
  }

  @Test
  void checkAnnotationSet() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new PostProcessorSetIgnore());
    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), schemas);
    final List<CsdlAnnotation> act = container.getEdmItem().getAnnotations();
    assertEquals(1, act.size());
    assertEquals("Capabilities.AsynchronousRequestsSupported", act.get(0).getTerm());
  }

  @Test
  void checkReturnEntitySetBasedOnInternalEntityType() throws ODataJPAModelException {

    final IntermediateEntityType<BestOrganization> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), getEntityType("BestOrganization"), schema);

    final IntermediateEntityContainer container = new IntermediateEntityContainer(new JPADefaultEdmNameBuilder(
        PUNIT_NAME),
        schemas);

    final JPAElement act = container.getEntitySet(et);
    assertNotNull(act);
    assertEquals("BestOrganizations", act.getExternalName());

  }

  private class PostProcessorSetIgnore extends JPAEdmMetadataPostProcessor {

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
    public void processEntityContainer(final IntermediateEntityContainerAccess container) {

      final CsdlConstantExpression mimeType = new CsdlConstantExpression(ConstantExpressionType.Bool, "false");
      final CsdlAnnotation annotation = new CsdlAnnotation();
      annotation.setExpression(mimeType);
      annotation.setTerm("Capabilities.AsynchronousRequestsSupported");
      final List<CsdlAnnotation> annotations = new ArrayList<>();
      annotations.add(annotation);
      container.addAnnotations(annotations);
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