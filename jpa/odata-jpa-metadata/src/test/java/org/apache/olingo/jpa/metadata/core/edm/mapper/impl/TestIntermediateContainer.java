package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.junit.Before;
import org.junit.Test;

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
    assertEquals(7, container.getEdmItem().getEntitySets().size());
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
}