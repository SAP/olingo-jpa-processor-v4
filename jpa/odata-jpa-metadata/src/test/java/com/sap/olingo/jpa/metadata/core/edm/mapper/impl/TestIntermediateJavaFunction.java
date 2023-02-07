package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaEmConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaOneFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaPrivateConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaTwoParameterConstructor;

class TestIntermediateJavaFunction extends TestMappingRoot {
  private TestHelper helper;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  void checkInternalNameEqualMethodName() throws ODataJPAModelException {
    final IntermediateFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertEquals("sum", act.getInternalName());
  }

  @Test
  void checkExternalNameEqualMethodName() throws ODataJPAModelException {
    final IntermediateFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertEquals("Sum", act.getExternalName());
  }

  @Test
  void checkReturnsConvertedPrimitiveReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getReturnType());
    assertEquals("Edm.Int32", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsConvertedPrimitiveParameterTypes() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
    assertEquals("Edm.Int16", act.getEdmItem().getParameter("A").getType());
    assertEquals("Edm.Int32", act.getEdmItem().getParameter("B").getType());
  }

  @Test
  void checkThrowsExceptionForNonPrimitiveParameter() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "errorNonPrimitiveParameter");

    assertThrows(ODataJPAModelException.class, () -> {
      act.getEdmItem();
    });
  }

  @Test
  void checkReturnsFalseForIsBound() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertEquals(false, act.getEdmItem().isBound());
  }

  @Test
  void checkReturnsTrueForHasFunctionImport() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertTrue(act.hasImport());
  }

  @Test
  void checkReturnsAnnotatedName() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertEquals("Add", act.getExternalName());
  }

  @Test
  void checkIgnoresGivenIsBound() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertFalse(act.getEdmItem().isBound());
    assertFalse(act.isBound());
  }

  @Test
  void checkIgnoresGivenHasFunctionImport() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertTrue(act.hasImport());
  }

  @Test
  void checkReturnsEnumerationTypeAsParameter() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEnumerationType");

    assertEquals("com.sap.olingo.jpa.AccessRights", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
    JPAParameter param = act.getParameter("arg0");
    if (param == null)
      param = act.getParameter("rights");
    assertNotNull(param);
    assertEquals("com.sap.olingo.jpa.AccessRights", param.getTypeFQN().getFullQualifiedNameAsString());
  }

  @Test
  void checkIgnoresParameterAsPartFromEdmFunction() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "div");

    assertNotNull(act.getEdmItem());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
  }

  @Test
  void checkThrowsExceptionIfAnnotatedReturnTypeNEDeclaredType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "errorReturnType");
    assertThrows(ODataJPAModelException.class, () -> {
      act.getEdmItem();
    });
  }

  @Test
  void checkReturnsFacetForNumbersOfReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "now");
    assertFalse(act.getEdmItem().getReturnType().isNullable());
    assertEquals(9, act.getEdmItem().getReturnType().getPrecision());
    assertEquals(3, act.getEdmItem().getReturnType().getScale());
  }

  @Test
  void checkReturnsFacetForStringsAndGeoOfReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "determineLocation");
    assertEquals(60, act.getEdmItem().getReturnType().getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getReturnType().getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getReturnType().getSrid().toString());
  }

  @Test
  void checkReturnsParameterFacetWithMapping() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertNotNull(act.getEdmItem().getParameters());
    assertNotNull(act.getEdmItem().getParameters().get(0).getMapping());
    assertEquals(Short.class, act.getEdmItem().getParameters().get(0).getMapping().getMappedJavaClass());
  }

  @Test
  void checkReturnsIsCollectionIfDefinedReturnTypeIsSubclassOfCollection() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnCollection");

    assertTrue(act.getEdmItem().getReturnType().isCollection());
    assertEquals("Edm.String", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkThrowsExceptionIfCollectionAndReturnTypeEmpty() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class,
        "returnCollectionWithoutReturnType");
    assertThrows(ODataJPAModelException.class, () -> {
      act.getEdmItem();
    });
  }

  @Test
  void checkReturnsEmbeddableTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEmbeddable");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsEmbeddableCollectionTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEmbeddableCollection");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  void checkReturnsEntityTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEntity");
    assertEquals("com.sap.olingo.jpa.Person", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsEnumerationTypeAsReturnType() throws ODataJPAModelException {

    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEnumerationType");
    assertEquals("com.sap.olingo.jpa.ABCClassification", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsEnumerationCollectionTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEnumerationCollection");

    assertEquals("com.sap.olingo.jpa.ABCClassification", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  void checkThrowsExceptionOnNotSupportedReturnType() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "wrongReturnType");
    assertThrows(ODataJPAModelException.class, () -> {
      act.getEdmItem();
    });
  }

  @Test
  void checkExceptConstructorWithoutParameter() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");
    act.getEdmItem();
  }

  @Test
  void checkExceptConstructorWithEntityManagerParameter() throws ODataJPAModelException {
    final IntermediateJavaFunction act = createFunction(ExampleJavaEmConstructor.class, "sum");
    act.getEdmItem();
  }

  @Test
  void checkThrowsExceptionOnPrivateConstructor() throws ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      createFunction(ExampleJavaPrivateConstructor.class, "sum");
    });
  }

  @Test
  void checkThrowsExceptionOnNoConstructorAsSpecified() throws ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      createFunction(ExampleJavaTwoParameterConstructor.class, "sum");
    });
  }

  private IntermediateJavaFunction createFunction(final Class<? extends ODataFunction> clazz, final String method)
      throws ODataJPAModelException {
    for (final Method m : Arrays.asList(clazz.getMethods())) {
      final EdmFunction functionDescription = m.getAnnotation(EdmFunction.class);
      if (functionDescription != null && method.equals(m.getName())) {
        return new IntermediateJavaFunction(new JPADefaultEdmNameBuilder(PUNIT_NAME), functionDescription, m,
            helper.schema);
      }
    }
    return null;
  }
}