package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.jpa.metadata.api.JPAEdmPostProcessor;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import org.junit.Before;
import org.junit.Test;

public class TestIntermediateDescriptionProperty extends TestMappingRoot {
  private TestHelper helper;
  private IntermediateDescriptionProperty cut;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    IntermediateModelElement.SetPostProcessor(new DefaultEdmPostProcessor());
  }

  @Test
  public void checkProptertyCanBeCreated() throws ODataJPAModelException {
    EmbeddableType<?> et = helper.getEmbeddedableType("PostalAddressData");
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, "countryName");
    new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema);
  }

  @Test
  public void checkGetProptertyNameOneToMany() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong name", "CountryName", cut.getEdmItem().getName());
  }

  @Test
  public void checkGetProptertyNameManyToMany() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "regionName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong name", "RegionName", cut.getEdmItem().getName());
  }

  @Test
  public void checkGetProptertyType() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals("Wrong type", EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString(),
        cut.getEdmItem().getType());
  }

  @Test
  public void checkGetProptertyIgnoreFalse() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediatePropertyAccess property = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME),
        jpaAttribute,
        helper.schema);
    assertFalse(property.ignore());
  }

  @Test
  public void checkGetProptertyFacetsNullableTrue() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertTrue(cut.getEdmItem().isNullable());
  }

  @Test
  public void checkGetProptertyMaxLength() throws ODataJPAModelException {
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);
    assertEquals(new Integer(100), cut.getEdmItem().getMaxLength());
  }

  @Test
  public void checkPostProcessorCalled() throws ODataJPAModelException {
    PostProcessorSpy spy = new PostProcessorSpy();
    IntermediateModelElement.SetPostProcessor(spy);
    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    cut.getEdmItem();
    assertTrue(spy.called);
  }

  @Test
  public void checkPostProcessorNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.SetPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    cut = new IntermediateDescriptionProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertEquals("Wrong name", "CountryDescription", cut.getEdmItem().getName());
  }

  @Test
  public void checkPostProcessorExternalNameChanged() throws ODataJPAModelException {
    PostProcessorSetName pPDouble = new PostProcessorSetName();
    IntermediateModelElement.SetPostProcessor(pPDouble);

    Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEmbeddedableType("PostalAddressData"),
        "countryName");
    IntermediatePropertyAccess property = new IntermediateProperty(new JPAEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema);

    assertEquals("Wrong name", "CountryDescription", property.getExternalName());
  }

  private class PostProcessorSpy extends JPAEdmPostProcessor {
    boolean called = false;

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      called = true;
    }
  }

  private class PostProcessorSetName extends JPAEdmPostProcessor {

    @Override
    public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {
      if (jpaManagedTypeClassName.equals(Addr_CANONICAL_NAME)) {
        if (property.getInternalName().equals("countryName")) {
          property.setExternalName("CountryDescription");
        }
      }
    }
  }
}
