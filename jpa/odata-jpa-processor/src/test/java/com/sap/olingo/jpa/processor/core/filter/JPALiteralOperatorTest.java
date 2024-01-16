package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.olingo.commons.core.edm.primitivetype.SingletonPrimitiveType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.util.ConfigurationBuilder;

class JPALiteralOperatorTest {
  private static OData odata = OData.newInstance();

  @TestFactory
  Stream<DynamicTest> testNonGeometryPrimitiveTypeAreConverted() {
    final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
    configBuilder.setScanners(new SubTypesScanner(false));
    configBuilder.forPackages(SingletonPrimitiveType.class.getPackage().getName());

    final Reflections reflection = new Reflections(configBuilder);
    final Set<Class<? extends SingletonPrimitiveType>> edmPrimitiveTypes = reflection.getSubTypesOf(
        SingletonPrimitiveType.class);

    return edmPrimitiveTypes
        .stream()
        .filter(type -> type.getSuperclass() == SingletonPrimitiveType.class)
        .map(JPALiteralOperatorTest::createEdmPrimitiveType)
        .filter(i -> i != null)
        .map(JPALiteralOperatorTest::createLiteralOperator)
        .map(operator -> dynamicTest(operator.getLiteral().getType().getName(), () -> assertTypeConversion(operator)));
  }

  private void assertTypeConversion(final JPALiteralOperator operator) throws ODataApplicationException {
    final Object act = operator.get();
    assertNotNull(act);
    assertFalse(act.toString().contains("'"));
  }

  private static SingletonPrimitiveType createEdmPrimitiveType(
      final Class<? extends SingletonPrimitiveType> typeClass) {
    try {
      final Method method = typeClass.getMethod("getInstance");
      return (SingletonPrimitiveType) method.invoke(null);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      return null;
    }
  }

  private static JPALiteralOperator createLiteralOperator(final SingletonPrimitiveType typeInstance) {
    final Literal literal = mock(Literal.class);
    when(literal.getType()).thenReturn(typeInstance);
    when(literal.getText()).thenReturn(determineLiteral(typeInstance));

    return new JPALiteralOperator(odata, literal);
  }

  private static String determineLiteral(final SingletonPrimitiveType typeInstance) {
    switch (typeInstance.getName()) {
      case "Guid":
        return "819a3e3b-837e-4ecb-a600-654ef7b5aace";
      case "Date":
        return "2021-10-01";
      case "Boolean":
        return "true";
      case "TimeOfDay":
        return "10:00:12.10";
      case "DateTimeOffset":
        return "2021-10-01T10:00:12Z";
      case "Duration":
        return "P2DT12H30M5S";
      default:
        return "123";
    }
  }
}
