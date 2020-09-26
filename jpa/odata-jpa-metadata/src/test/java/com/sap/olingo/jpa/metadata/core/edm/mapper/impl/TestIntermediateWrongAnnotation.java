package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_MISSING_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_WRONG_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_KEY_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MANDATORY_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_PROTECTED_COLLECTION;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_PROTECTED_NAVIGATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.errormodel.CollectionAttributeProtected;
import com.sap.olingo.jpa.processor.core.errormodel.ComplextProtectedNoPath;
import com.sap.olingo.jpa.processor.core.errormodel.ComplextProtectedWrongPath;
import com.sap.olingo.jpa.processor.core.errormodel.EmbeddedKeyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.KeyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.MandatoryPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.NavigationAttributeProtected;
import com.sap.olingo.jpa.processor.core.errormodel.NavigationPropertyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.PersonDeepCollectionProtected;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

public class TestIntermediateWrongAnnotation {
  private TestHelper helper;
  protected static final String PUNIT_NAME = "error";
  protected static EntityManagerFactory emf;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkErrorOnProtectedCollectionAttribute() {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        CollectionAttributeProtected.class), "inhouseAddress");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(CollectionAttributeProtected.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            jpaAttribute, helper.schema, entityType));

    assertEquals(NOT_SUPPORTED_PROTECTED_COLLECTION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());

  }

  @Test
  public void checkErrorOnProtectedCollectionAttributeDeep() {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        PersonDeepCollectionProtected.class), "inhouseAddress");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(PersonDeepCollectionProtected.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateCollectionProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            jpaAttribute, helper.schema, entityType));

    assertEquals(NOT_SUPPORTED_PROTECTED_COLLECTION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnProtectedNavigationAttribute() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        NavigationAttributeProtected.class), "teams");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            helper.schema.getEntityType(NavigationAttributeProtected.class), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_PROTECTED_NAVIGATION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnProtectedComplexAttributeMissingPath() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        ComplextProtectedNoPath.class),
        "administrativeInformation");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(COMPLEX_PROPERTY_MISSING_PROTECTION_PATH.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnProtectedComplexAttributeWrongPath() throws ODataJPAModelException {
    // ComplextProtectedWrongPath
    final EntityType<ComplextProtectedWrongPath> jpaEt = helper.getEntityType(ComplextProtectedWrongPath.class);
    final IntermediateEntityType<ComplextProtectedWrongPath> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaEt, helper.schema);
    et.getEdmItem();

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> et.getProtections());

    assertEquals(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());

  }

  @Test
  public void checkErrorOnNavigationPropertyPartOfGroup() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        NavigationPropertyPartOfGroup.class), "teams");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(NavigationPropertyPartOfGroup.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), entityType, jpaAttribute,
            helper.schema));

    assertEquals(NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnMandatoryPropertyPartOfGroup() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        MandatoryPartOfGroup.class), "eTag");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_MANDATORY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnKeyPropertyPartOfGroup() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        KeyPartOfGroup.class), "iD");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_KEY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  public void checkErrorOnEmbeddedKeyPropertyPartOfGroup() throws ODataJPAModelException {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        EmbeddedKeyPartOfGroup.class), "key");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_KEY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }
}
