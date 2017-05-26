package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

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

  private IntermediateJavaFunction createFunction(Class<ExampleJavaOneFunction> clazz, String method)
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
