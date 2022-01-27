package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class JPANavigationOperationTest {
  private JPANavigationOperation cut;

  private JPAFilterComplierAccess jpaComplier;
  private MethodKind methodCall;
  private List<JPAOperator> parameters;
  private JPAMemberOperator operator;

  @BeforeEach
  void setup() {
    jpaComplier = mock(JPAFilterComplierAccess.class);
    operator = mock(JPAMemberOperator.class);
    methodCall = MethodKind.INDEXOF;
    parameters = new ArrayList<>();
    parameters.add(operator);
    cut = new JPANavigationOperation(jpaComplier, methodCall, parameters);

  }

  @Test
  void testCreateParameterOneLiteral() {
    final JPALiteralOperator literal = mock(JPALiteralOperator.class);
    parameters.add(0, literal);
    assertDoesNotThrow(() -> new JPANavigationOperation(jpaComplier, methodCall, parameters));
  }

  @Test
  void testGetNameOfMethod() {
    assertEquals(methodCall.name(), cut.getName());
  }

  @Test
  void testToStringContainsMethodCall() {
    assertTrue(cut.toString().contains(methodCall.toString()));
  }

  @Test
  void testReturnNullOfSubMember() {
    final Member act = cut.getMember();
    assertNotNull(act);
  }

  @TestFactory
  Stream<DynamicTest> testMemberMethodsReturninfNull() {
    final Member act = cut.getMember();
    return Stream.of("getStartTypeFilter", "getType")
        .map(method -> dynamicTest(method, () -> assertNull(executeMethod(act, method))));
  }

  @Test
  void testMemberAcceptReturtnsNull() throws ExpressionVisitException, ODataApplicationException {
    assertNull(cut.getMember().accept(null));
  }

  @Test
  void testMemberIsCollectionFalse() {
    assertFalse(cut.getMember().isCollection());
  }

  @TestFactory
  Stream<DynamicTest> testMemberResourceMethodsReturninfNull() {
    final UriInfoResource act = cut.getMember().getResourcePath();
    return Stream.of("getApplyOption", "getCountOption", "getDeltaTokenOption", "getExpandOption", "getFilterOption",
        "getFormatOption", "getIdOption", "getOrderByOption", "getSearchOption", "getSelectOption", "getSkipOption",
        "getSkipTokenOption", "getTopOption")
        .map(method -> dynamicTest(method, () -> assertNull(executeMethod(act, method))));
  }

  @Test
  void testMemberResourceGetValueForAliasNull() {
    assertNull(cut.getMember().getResourcePath().getValueForAlias("Test"));
  }

  @Test
  void testMemberResourceGetCustomQueryOptionReturnsEmptyList() {
    final List<CustomQueryOption> act = cut.getMember().getResourcePath().getCustomQueryOptions();

    assertNotNull(act);
    assertTrue(act.isEmpty());
  }

  @TestFactory
  Stream<DynamicTest> testMethodsReturninfNull() {
    return Stream.of("getOperator")
        .map(method -> dynamicTest(method, () -> assertNull(executeMethod(cut, method))));
  }

  private Object executeMethod(final Object obj, final String methodName) {
    final Class<?> clazz = obj.getClass();
    try {
      final Method method = clazz.getMethod(methodName);
      return method.invoke(obj);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      fail();
    }
    return "Test";
  }

}
