package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_MISSING_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.COMPLEX_PROPERTY_WRONG_PROTECTION_PATH;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_KEY_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MANDATORY_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_PROTECTED_COLLECTION;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_PROTECTED_NAVIGATION;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.PROPERTY_REQUIRED_UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.errormodel.CollectionAttributeProtected;
import com.sap.olingo.jpa.processor.core.errormodel.ComplexProtectedNoPath;
import com.sap.olingo.jpa.processor.core.errormodel.ComplexProtectedWrongPath;
import com.sap.olingo.jpa.processor.core.errormodel.EmbeddedKeyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.KeyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.MandatoryPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.NavigationAttributeProtected;
import com.sap.olingo.jpa.processor.core.errormodel.NavigationPropertyPartOfGroup;
import com.sap.olingo.jpa.processor.core.errormodel.PersonDeepCollectionProtected;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientError;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class IntermediateWrongAnnotationTest {
  private TestHelper helper;
  protected static final String PUNIT_NAME = "error";
  protected static EntityManagerFactory emf;
  protected IntermediateAnnotationInformation annotationInfo;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  void checkErrorOnProtectedCollectionAttribute() {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        CollectionAttributeProtected.class), "inhouseAddress");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(
        CollectionAttributeProtected.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateCollectionProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            jpaAttribute, helper.schema, entityType));

    assertEquals(NOT_SUPPORTED_PROTECTED_COLLECTION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());

  }

  @Test
  void checkErrorOnProtectedCollectionAttributeDeep() {
    final PluralAttribute<?, ?, ?> jpaAttribute = helper.getCollectionAttribute(helper.getEntityType(
        PersonDeepCollectionProtected.class), "inhouseAddress");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(PersonDeepCollectionProtected.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateCollectionProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            jpaAttribute, helper.schema, entityType));

    assertEquals(NOT_SUPPORTED_PROTECTED_COLLECTION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnProtectedNavigationAttribute() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        NavigationAttributeProtected.class), "teams");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
            helper.schema.getEntityType(NavigationAttributeProtected.class), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_PROTECTED_NAVIGATION.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnProtectedComplexAttributeMissingPath() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        ComplexProtectedNoPath.class),
        "administrativeInformation");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(COMPLEX_PROPERTY_MISSING_PROTECTION_PATH.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnProtectedComplexAttributeWrongPath() throws ODataJPAModelException {
    // ComplexProtectedWrongPath
    final EntityType<ComplexProtectedWrongPath> jpaEt = helper.getEntityType(ComplexProtectedWrongPath.class);
    final IntermediateEntityType<ComplexProtectedWrongPath> et = new IntermediateEntityType<>(
        new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaEt, helper.schema);
    et.getEdmItem();

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        et::getProtections);

    assertEquals(COMPLEX_PROPERTY_WRONG_PROTECTION_PATH.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());

  }

  @Test
  void checkErrorOnNavigationPropertyPartOfGroup() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        NavigationPropertyPartOfGroup.class), "teams");
    final IntermediateStructuredType<?> entityType = helper.schema.getEntityType(NavigationPropertyPartOfGroup.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME), entityType, jpaAttribute,
            helper.schema));

    assertEquals(NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnMandatoryPropertyPartOfGroup() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        MandatoryPartOfGroup.class), "eTag");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_MANDATORY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnKeyPropertyPartOfGroup() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        KeyPartOfGroup.class), "iD");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_KEY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnEmbeddedKeyPropertyPartOfGroup() {
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(helper.getEntityType(
        EmbeddedKeyPartOfGroup.class), "key");

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new IntermediateSimpleProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute, helper.schema));

    assertEquals(NOT_SUPPORTED_KEY_PART_OF_GROUP.name(), act.getId());
    assertFalse(act.getMessage().isEmpty());
  }

  @Test
  void checkErrorOnTransientFieldWithUnknownRequired() {
    final EntityType<TeamWithTransientError> jpaEt = helper.getEntityType(TeamWithTransientError.class);
    final IntermediateEntityType<TeamWithTransientError> et = new IntermediateEntityType<>(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), jpaEt, helper.schema);
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        et::getEdmItem);
    assertEquals(PROPERTY_REQUIRED_UNKNOWN.name(), act.getId());
  }
}
