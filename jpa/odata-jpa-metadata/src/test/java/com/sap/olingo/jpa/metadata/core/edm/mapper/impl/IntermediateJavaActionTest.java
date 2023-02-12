package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testaction.ActionWithOverload;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaActions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaEmConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaPrivateConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaTwoParameterConstructor;

class IntermediateJavaActionTest extends TestMappingRoot {
  private TestHelper helper;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  void checkInternalNameEqualMethodName() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("unboundWithImport", act.getInternalName());
  }

  @Test
  void checkInternalNameGiven() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("unboundWithImport", act.getInternalName());
  }

  @Test
  void checkExternalNameEqualMethodName() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("UnboundWithImport", act.getExternalName());
  }

  @Test
  void checkReturnsFalseForIsBound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertFalse(act.getEdmItem().isBound());
  }

  @Test
  void checkReturnsTrueForIsBound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals(PUNIT_NAME + ".Person", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkReturnsEntitySetPathForBound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundWithEntitySetPath");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals("Person/Roles", act.getEdmItem().getEntitySetPath());
  }

  @Test
  void checkReturnsGivenEntitySetTypeIfBound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals(PUNIT_NAME + ".Person", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
    assertEquals("Edm.Decimal", act.getEdmItem().getParameters().get(1).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkReturnsExternalName() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertEquals("BoundNoImport", act.getEdmItem().getName());
  }

  @Test
  void checkReturnsTrueForHasActionImportIfUnbound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertTrue(act.hasImport());
  }

  @Test
  void checkReturnsFalseForHasActionImportIfNotSet() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertFalse(act.hasImport());
  }

  @Test
  void checkEmptyParameterNameThrowsException() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "nameEmpty");

    assertThrows(ODataJPAModelException.class, () -> act.getParameter());
  }

  @Test
  void checkReturnsReturnTypeConvertedPrimitiveReturnType() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getReturnType());
    assertEquals("Edm.Int32", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsReturnTypeNullForVoid() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertNull(act.getEdmItem().getReturnType());
  }

  @Test
  void checkReturnsReturnTypeEmbeddableType() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEmbeddable");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsEntityTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEntity");

    assertEquals("com.sap.olingo.jpa.Person", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsEnumerationTypeAsReturnType() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEnumeration");

    assertEquals("com.sap.olingo.jpa.ABCClassification", act.getEdmItem().getReturnType().getType());
  }

  @Test
  void checkReturnsReturnTypeCollectionOfPrimitive() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnCollection");

    assertEquals("Edm.String", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  void checkReturnsReturnTypeCollectionOfEmbeddable() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEmbeddableCollection");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  void checkReturnsReturnTypeFacetForNumbers() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundReturnFacet");
    assertFalse(act.getEdmItem().getReturnType().isNullable());
    assertEquals(20, act.getEdmItem().getReturnType().getPrecision());
    assertEquals(5, act.getEdmItem().getReturnType().getScale());
  }

  @Test
  void checkReturnsReturnTypeFacetForNonNumbers() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNull(act.getEdmItem().getReturnType().getPrecision());
    assertNull(act.getEdmItem().getReturnType().getScale());
    assertNull(act.getEdmItem().getReturnType().getMaxLength());
  }

  @Test
  void checkReturnsReturnTypeFacetForStringsAndGeo() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "calculateLocation");

    assertEquals(60, act.getEdmItem().getReturnType().getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getReturnType().getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getReturnType().getSrid().toString());
  }

  @Test
  void checkReturnsParameterConvertPrimitiveTypes() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
    assertEquals("Edm.Int16", act.getEdmItem().getParameter("A").getType());
    assertEquals("Edm.Int32", act.getEdmItem().getParameter("B").getType());
  }

  @Test
  void checkReturnsParameterFacetForNumbers() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getParameter());
    assertEquals(34, act.getParameter().get(1).getPrecision());
    assertEquals(10, act.getParameter().get(1).getScale());

    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(34, act.getEdmItem().getParameters().get(1).getPrecision());
    assertEquals(10, act.getEdmItem().getParameters().get(1).getScale());
  }

  @Test
  void checkReturnsParameterFacetForNonNumbers() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem().getParameters());
    assertNull(act.getEdmItem().getParameters().get(1).getPrecision());
    assertNull(act.getEdmItem().getParameters().get(1).getScale());
    assertNull(act.getEdmItem().getParameters().get(1).getMaxLength());
  }

  @Test
  void checkReturnsParameterFacetForStringsAndGeo() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "calculateLocation");

    assertNotNull(act.getParameter());
    assertEquals(100, act.getParameter().get(0).getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getParameter().get(0).getSrid().getDimension());
    assertEquals("4326", act.getParameter().get(0).getSrid().toString());

    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(100, act.getEdmItem().getParameters().get(0).getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getParameters().get(0).getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getParameters().get(0).getSrid().toString());
  }

  @Test
  void checkReturnsParameterFacetWithMapping() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundReturnFacet");

    assertNotNull(act.getEdmItem().getParameters());
    assertNotNull(act.getEdmItem().getParameters().get(0).getMapping());
    assertEquals(Short.class, act.getEdmItem().getParameters().get(0).getMapping().getMappedJavaClass());
  }

  @Test
  void checkReturnsEnumerationTypeAsParameter() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEnumeration");

    assertEquals("com.sap.olingo.jpa.AccessRights", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  void checkProvidesAllParameter() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");
    final List<JPAParameter> actParams = act.getParameter();
    assertEquals(2, actParams.size());
  }

  @Test
  void checkProvidesParameterByDeclared() throws ODataJPAModelException, NoSuchMethodException,
      SecurityException {

    final Method m = ExampleJavaActions.class.getMethod("unboundWithImport", short.class, int.class);
    final Parameter[] params = m.getParameters();
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");
    assertNotNull(act.getParameter(params[0]));
    assertEquals("A", act.getParameter(params[0]).getName());
    assertNotNull(act.getParameter(params[1]));
    assertEquals("B", act.getParameter(params[1]).getName());
  }

  @Test
  void checkExceptConstructorWithoutParameter() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");
    act.getEdmItem();
    assertNotNull(act.getConstructor());
  }

  @Test
  void checkExceptConstructorWithEntityManagerParameter() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaEmConstructor.class, "mul");

    assertNotNull(act.getConstructor());
    assertEquals(1, act.getConstructor().getParameterTypes().length);
  }

  @Test
  void checkThrowsExceptionOnPrivateConstructor() throws ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      createAction(ExampleJavaPrivateConstructor.class, "mul");
    });
  }

  @Test
  void checkThrowsExceptionOnNoConstructorAsSpecified() throws ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      createAction(ExampleJavaTwoParameterConstructor.class, "mul");
    });
  }

  @TestFactory
  Stream<DynamicTest> testCreateActionThrowsException() {

    final Class<ExampleJavaActions> clazz = ExampleJavaActions.class;
    return Stream.of("errorNonPrimitiveParameter",
        "returnCollectionWithoutReturnType",
        "boundWithOutBindingParameter",
        "boundWithOutParameter",
        "boundBindingParameterSecondParameter",
        "errorUnboundWithEntitySetPath",
        "errorPrimitiveTypeWithEntitySetPath")
        .map(name -> createActionNoThrow(clazz, name))
        .map(function -> dynamicTest(function.internalName,
            () -> assertThrows(ODataJPAModelException.class, () -> function.getEdmItem())

        ));
  }

  @Test
  void checkGetReturnTypeReturnsCsdlReturnType() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEntity");
    act.getEdmItem();
    assertNotNull(act.getReturnType());
    assertEquals("Person", act.getReturnType().getTypeFQN().getName());
  }

  @Test
  void checkGetReturnTypeReturnsNullForVoid() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");
    act.getEdmItem();
    assertNull(act.getReturnType());
  }

  @Test
  void checkIsBoundReturnsTrueForBound() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");
    act.getEdmItem();
    assertTrue(act.isBound());
  }

  @Test
  void checkConstructorWithThreeParameter() throws ODataJPAModelException {
    final IntermediateJavaAction act = createAction(ActionWithOverload.class, "baseAction");
    assertNotNull(act.getConstructor());
    assertEquals(3, act.getConstructor().getParameterCount());
  }

  private IntermediateJavaAction createAction(final Class<? extends ODataAction> clazz, final String method)
      throws ODataJPAModelException {
    for (final Method m : Arrays.asList(clazz.getMethods())) {
      final EdmAction actionDescription = m.getAnnotation(EdmAction.class);
      if (actionDescription != null && method.equals(m.getName())) {
        return new IntermediateJavaAction(new JPADefaultEdmNameBuilder(PUNIT_NAME), actionDescription, m,
            helper.schema);
      }
    }
    return null;
  }

  private IntermediateJavaAction createActionNoThrow(final Class<? extends ODataAction> clazz, final String method) {
    for (final Method m : Arrays.asList(clazz.getMethods())) {
      final EdmAction actionDescription = m.getAnnotation(EdmAction.class);
      if (actionDescription != null && method.equals(m.getName())) {
        try {
          return new IntermediateJavaAction(new JPADefaultEdmNameBuilder(PUNIT_NAME), actionDescription, m,
              helper.schema);
        } catch (final ODataJPAModelException e) {
          fail(e.getMessage());
        }
      }
    }
    return null;
  }
}
