package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.JoinTable;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.JoinSource;

class IntermediateJoinTableTest extends TestMappingRoot {

  private IntermediateSchema schema;
  private TestHelper helper;
  private JoinTable jpaJoinTable;
  private IntermediateAnnotationInformation annotationInfo;
  private IntermediateNavigationProperty<?> property;
  private IntermediateJoinTable cut;

  @BeforeEach
  void setup() throws ODataJPAModelException, NoSuchFieldException, SecurityException {
    final Reflections reflections = mock(Reflections.class);
    annotationInfo = new IntermediateAnnotationInformation(new ArrayList<>());
    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), reflections,
        annotationInfo);
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    jpaJoinTable = determineJoinTable(JoinSource.class, "oneToMany");
    property = buildNavigationProperty(JoinSource.class, "oneToMany");
  }

  private <S> IntermediateNavigationProperty<S> buildNavigationProperty(final Class<S> clazz,
      final String attribute) throws ODataJPAModelException {
    final EntityType<S> et = helper.getEntityType(clazz);
    final IntermediateStructuredType<S> st = schema.getEntityType(clazz);
    final Attribute<?, ?> jpaAttribute = helper.getDeclaredAttribute(et, attribute);
    return new IntermediateNavigationProperty<>(new JPADefaultEdmNameBuilder(PUNIT_NAME),
        st, jpaAttribute, schema);
  }

  private JoinTable determineJoinTable(final Class<JoinSource> clazz, final String attribute)
      throws NoSuchFieldException, SecurityException {
    return clazz.getDeclaredField(attribute).getAnnotation(JoinTable.class);
  }

  @Test
  void checkCreateJoinTable() {
    assertNotNull(new IntermediateJoinTable(property, jpaJoinTable, schema));
  }

  @Test
  void checkGetLeftColumns() throws ODataJPAModelException {
    property.getEdmItem();
    cut = new IntermediateJoinTable(property, jpaJoinTable, schema);
    final List<JPAPath> act = cut.getLeftColumnsList();
    assertNotNull(act);
    assertFalse(act.isEmpty());
    assertEquals("\"SourceKey\"", act.get(0).getDBFieldName());
  }

  @Test
  void checkGetRightColumns() throws ODataJPAModelException {
    cut = new IntermediateJoinTable(property, jpaJoinTable, schema);
    final List<JPAPath> act = cut.getRightColumnsList();
    assertNotNull(act);
    assertFalse(act.isEmpty());
    assertEquals("\"TargetKey\"", act.get(0).getDBFieldName());
  }
}
