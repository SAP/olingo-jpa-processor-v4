package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

public class TestIntermediateJavaFunction extends TestMappingRoot {
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkInternalNameEqualMethodName() throws ODataJPAModelException {
    IntermediateFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertEquals("sum", act.getInternalName());
  }

  @Test
  public void checkExternalNameEqualMethodName() throws ODataJPAModelException {
    IntermediateFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertEquals("Sum", act.getExternalName());
  }

  @Test
  public void checkReturnsConvertedPrimitiveReturnType() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getReturnType());
    assertEquals("Edm.Int32", act.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkReturnsConvertedPrimitiveParameterTypes() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
    assertEquals("Edm.Int16", act.getEdmItem().getParameter("A").getType());
    assertEquals("Edm.Int32", act.getEdmItem().getParameter("B").getType());
  }

  @Test
  public void checkReturnsFalseForIsBound() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertNotNull(act.getEdmItem());
    assertEquals(false, act.getEdmItem().isBound());
  }

  @Test
  public void checkReturnsTrueForHasFunctionImport() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaOneFunction.class, "sum");

    assertTrue(act.hasFunctionImport());
  }

  @Test
  public void checkReturnsAnnotatedName() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertEquals("Add", act.getExternalName());
  }

  @Test
  public void checkIgnoresGivenIsBound() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertEquals(false, act.getEdmItem().isBound());
  }

  @Test
  public void checkIgnoresGivenHasFunctionImport() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "sum");

    assertTrue(act.hasFunctionImport());
  }

  @Test
  public void checkIgnoresParameterAsPartFromEdmFunction() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "div");

    assertNotNull(act.getEdmItem());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsErrorIfAnnotatedReturnTypeNEDeclairedType() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "errorReturnType");
    act.getEdmItem();
  }

  @Test
  public void checkReturnsFacetForNumbersOfReturnType() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "now");
    assertFalse(act.getEdmItem().getReturnType().isNullable());
    assertEquals(Integer.valueOf(9), act.getEdmItem().getReturnType().getPrecision());
    assertEquals(Integer.valueOf(3), act.getEdmItem().getReturnType().getScale());
  }

  @Test
  public void checkReturnsFacetForStringsAndGeoOfReturnType() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "determineLocation");
    assertEquals(Integer.valueOf(60), act.getEdmItem().getReturnType().getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getReturnType().getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getReturnType().getSrid().toString());
  }

  @Test
  public void checkReturnsIsCollectionIfDefinedReturnTypeIsSubclassOfCollection() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnCollection");

    assertTrue(act.getEdmItem().getReturnType().isCollection());
    assertEquals("Edm.String", act.getEdmItem().getReturnType().getType());
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExceptionIfCollectionAndReturnTypeEmpty() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnCollectionWithoutReturnType");
    act.getEdmItem();
  }

  @Test
  public void checkReturnsEmbeddableTypeAsReturnType() throws ODataJPAModelException {
    IntermediateJavaFunction act = createFunction(ExampleJavaFunctions.class, "returnEmbeddable");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
  }

  private IntermediateJavaFunction createFunction(Class<? extends ODataFunction> clazz, String method)
      throws ODataJPAModelException {
    for (Method m : Arrays.asList(clazz.getMethods())) {
      EdmFunction functionDescribtion = m.getAnnotation(EdmFunction.class);
      if (functionDescribtion != null && method.equals(m.getName())) {
        return new IntermediateJavaFunction(new JPAEdmNameBuilder(PUNIT_NAME), functionDescribtion, m, helper.schema);
      }
    }
    return null;
  }
}
