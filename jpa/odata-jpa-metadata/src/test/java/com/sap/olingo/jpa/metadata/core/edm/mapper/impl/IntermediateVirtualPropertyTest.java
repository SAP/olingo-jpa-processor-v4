package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;

class IntermediateVirtualPropertyTest extends TestMappingRoot {
  private IntermediateSchema schema;
  private IntermediateVirtualProperty cut;
  private Attribute<?, ?> jpaAttribute;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    final Reflections r = mock(Reflections.class);
    schema = new IntermediateSchema(new JPADefaultEdmNameBuilder(PUNIT_NAME), emf.getMetamodel(), r);
    jpaAttribute = mock(Attribute.class);
    when(jpaAttribute.getJavaMember()).thenReturn(null);
    when(jpaAttribute.getName()).thenReturn("DummyName");
    when(jpaAttribute.getDeclaringType()).thenAnswer(new Answer<ManagedType<?>>() {
      @Override
      public ManagedType<?> answer(final InvocationOnMock invocation) throws Throwable {
        return getEntityType(DummyToBeIgnored.class);
      }
    });

    cut = new IntermediateVirtualProperty(nameBuilder, jpaAttribute, schema, "TestColumn", Integer.class);

  }

  @Test
  void checkConstructor() {
    assertNotNull(cut);
  }

  @TestFactory
  Iterable<DynamicTest> checkReturnsFixValue() {
    return Arrays.asList(
        dynamicTest("isEnum returns false", () -> assertFalse(cut.isEnum())),
        dynamicTest("isEtag returns false", () -> assertFalse(cut.isEtag())),
        dynamicTest("isSearchable returns false", () -> assertFalse(cut.isSearchable())),
        dynamicTest("isTransient returns false", () -> assertFalse(cut.isTransient())),
        dynamicTest("ignore returns true", () -> assertTrue(cut.ignore())),
        dynamicTest("isAssociation returns false", () -> assertFalse(cut.isAssociation())),
        dynamicTest("isCollection returns false", () -> assertFalse(cut.isCollection())),
        dynamicTest("isComplex returns false", () -> assertFalse(cut.isComplex())),
        dynamicTest("isKey returns false", () -> assertFalse(cut.isKey())),
        dynamicTest("isStream returns false", () -> assertFalse(cut.isStream())),
        dynamicTest("isPartOfGroup returns false", () -> assertFalse(cut.isPartOfGroup())));
  }

  @TestFactory
  Iterable<DynamicTest> checkReturnsNull() {
    return Arrays.asList(
        dynamicTest("determineType returns null", () -> assertNull(cut.determineType())),
        dynamicTest("getStructuredType returns null", () -> assertNull(cut.getStructuredType())),
        dynamicTest("getDefaultValue returns null", () -> assertNull(cut.getDefaultValue())));
  }

  @Test
  void testGetDbType() {
    assertEquals(Integer.class, cut.getDbType());
  }

  @Test
  void testGetType() {
    assertEquals(Integer.class, cut.getType());
  }

  @Test
  void testGetJavaType() {
    assertEquals(Integer.class, cut.getJavaType());
  }

  @Test
  void testDetermineIsVersion() {
    cut.determineIsVersion();
    assertFalse(cut.isEtag());
  }

  @Test
  void testType() throws ODataJPAModelException {
    cut.checkConsistency();
    cut.determineStreamInfo();
    cut.determineStructuredType();
    assertNull(cut.getStructuredType());
  }
}
