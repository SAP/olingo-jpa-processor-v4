package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.DefaultEdmPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateEntityContainer;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateModelElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateSchema;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;

public class TestIntermediateContainer extends TestMappingRoot {
  private HashMap<String, IntermediateSchema> schemas = new HashMap<String, IntermediateSchema>();

  @Before
  public void setup() throws ODataJPAModelException {
    IntermediateModelElement.setPostProcessor(new DefaultEdmPostProcessor());
    IntermediateSchema schema = new IntermediateSchema(new JPAEdmNameBuilder(PUNIT_NAME), emf.getMetamodel());
    schemas.put(PUNIT_NAME, schema);
  }

  @Test
  public void checkContainerCanBeCreated() throws ODataJPAModelException {

    new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);
  }

  @Test
  public void checkGetName() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);
    assertEquals("OrgApacheOlingoJpaContainer", container.getExternalName());
  }

  @Test
  public void checkGetNoEntitySets() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);
    assertEquals(TestDataConstants.NO_ENTITY_TYPES, container.getEdmItem().getEntitySets().size());
  }

  @Test
  public void checkGetEntitySetName() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);
    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) return;
    }
    fail();
  }

  @Test
  public void checkGetEntitySetType() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);
    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        assertEquals(new JPAEdmNameBuilder(PUNIT_NAME).buildFQN("BusinessPartner"), entitySet.getTypeFQN());
        return;
      }
    }
    fail();
  }

  @Test
  public void checkGetNoNavigationPropertyBindings() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        assertEquals(4, entitySet.getNavigationPropertyBindings().size());
        return;
      }
    }
    fail();
  }

  @Test
  public void checkGetNavigationPropertyBindingsPath() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("Roles".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  public void checkGetNavigationPropertyBindingsTarget() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
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
  public void checkGetNavigationPropertyBindingsPathComplexType() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("Address/AdministrativeDivision".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  public void checkGetNavigationPropertyBindingsPathComplexTypeNested() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlEntitySet> entitySets = container.getEdmItem().getEntitySets();
    for (CsdlEntitySet entitySet : entitySets) {
      if (entitySet.getName().equals("BusinessPartners")) {
        for (CsdlNavigationPropertyBinding binding : entitySet.getNavigationPropertyBindings()) {
          if ("AdministrativeInformation/Created/User".equals(binding.getPath()))
            return;
        }
      }
    }
    fail();
  }

  @Test
  public void checkGetNoFunctionImportIfBound() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("CountRoles")) {
        fail("Bound function must not generate a function import");
      }
    }
  }

  @Test
  public void checkGetNoFunctionImportIfUnBoundHasImportFalse() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("max")) {
        fail("UnBound function must not generate a function import is not annotated");
      }
    }
  }

  @Test
  public void checkGetFunctionImportIfUnBoundHasImportTrue() throws ODataJPAModelException {

    IntermediateEntityContainer container = new IntermediateEntityContainer(new JPAEdmNameBuilder(PUNIT_NAME), schemas);

    List<CsdlFunctionImport> funcImports = container.getEdmItem().getFunctionImports();
    for (CsdlFunctionImport funcImport : funcImports) {
      if (funcImport.getName().equals("Olingo V4 ")) {
        fail("UnBound function must be generate a function import is annotated");
      }
    }
  }
}