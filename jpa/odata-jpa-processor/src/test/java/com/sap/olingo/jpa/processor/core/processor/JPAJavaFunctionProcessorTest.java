package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testobjects.FileAccess;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionActionConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionForFilter;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionParameter;

class JPAJavaFunctionProcessorTest {

  private JPAJavaFunctionProcessor cut;

  private JPAODataQueryContext queryContext;
  private UriResourceFunction uriResourceFunction;
  private JPAJavaFunction jpaFunction;
  private EdmFunction edmFunction;
  private JPAServiceDocument sd;
  private List<UriParameter> uriParameters;
  private EntityManager em;
  private JPAHttpHeaderMap header;
  private JPARequestParameterMap requestParameter;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    uriParameters = new ArrayList<>();
    sd = mock(JPAServiceDocument.class);
    jpaFunction = mock(JPAJavaFunction.class);
    uriResourceFunction = mock(UriResourceFunction.class);
    queryContext = mock(JPAODataQueryContext.class);
    em = mock(EntityManager.class);
    header = mock(JPAHttpHeaderMap.class);
    requestParameter = mock(JPARequestParameterMap.class);
    edmFunction = mock(EdmFunction.class);
    when(uriResourceFunction.getFunction()).thenReturn(edmFunction);
  }

  @Test
  void testConstructorQueryContextParameter() throws ODataApplicationException, NoSuchMethodException,
      SecurityException, ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionForFilter> c = TestFunctionForFilter.class.getConstructor(JPAODataQueryContext.class);
    final Method m = TestFunctionForFilter.class.getMethod("at2", LocalDate.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("date", m);
    setPrimitiveValue(LocalDate.now(), parameter);
    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, queryContext);
    assertDoesNotThrow(() -> cut.process());
  }

  @Test
  void testConstructorWithThreeParameter() throws ODataApplicationException, NoSuchMethodException, SecurityException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionActionConstructor> c = TestFunctionActionConstructor.class.getConstructor(
        EntityManager.class, JPAHttpHeaderMap.class, JPARequestParameterMap.class);
    final Method m = TestFunctionActionConstructor.class.getMethod("func", LocalDate.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("date", m);
    setPrimitiveValue(LocalDate.now(), parameter);

    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, em, header, requestParameter,
        queryContext);
    assertTrue((Boolean) cut.process());
  }

  @Test
  void testParameterNull() throws ODataApplicationException, NoSuchMethodException, SecurityException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionActionConstructor> c = TestFunctionActionConstructor.class.getConstructor(
        EntityManager.class, JPAHttpHeaderMap.class, JPARequestParameterMap.class);
    final Method m = TestFunctionActionConstructor.class.getMethod("func", LocalDate.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("date", m);
    setPrimitiveValue(null, parameter);

    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, em, header, requestParameter,
        queryContext);
    assertTrue((Boolean) cut.process());
  }

  @Test
  void testParameterEnum() throws ODataApplicationException, NoSuchMethodException, SecurityException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionActionConstructor> c = TestFunctionActionConstructor.class.getConstructor(
        EntityManager.class, JPAHttpHeaderMap.class, JPARequestParameterMap.class);
    final Method m = TestFunctionActionConstructor.class.getMethod("funcEnum", FileAccess.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("access", m);
    setEnumValue(FileAccess.Write, parameter, true);

    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("access")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, em, header, requestParameter,
        queryContext);
    assertTrue((Boolean) cut.process());
  }

  @Test
  void testThrowsExceptionEnumNotFound() throws ODataApplicationException, NoSuchMethodException, SecurityException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionActionConstructor> c = TestFunctionActionConstructor.class.getConstructor(
        EntityManager.class, JPAHttpHeaderMap.class, JPARequestParameterMap.class);
    final Method m = TestFunctionActionConstructor.class.getMethod("funcEnum", FileAccess.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("access", m);
    setEnumValue(FileAccess.Write, parameter, false);

    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("access")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, em, header, requestParameter,
        queryContext);
    assertThrows(ODataJPAProcessorException.class, () -> cut.process());
  }

  @Test
  void testThrowsExceptionOnUnsupportedType() throws ODataApplicationException, NoSuchMethodException,
      SecurityException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionActionConstructor> c = TestFunctionActionConstructor.class.getConstructor(
        EntityManager.class, JPAHttpHeaderMap.class, JPARequestParameterMap.class);
    final Method m = TestFunctionActionConstructor.class.getMethod("funcEnum", FileAccess.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("access", m);
    setComplexValue(m, parameter);

    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("access")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, em, header, requestParameter,
        queryContext);
    assertThrows(ODataJPADBAdaptorException.class, () -> cut.process());
  }

  @Test
  void testRethrowsExceptionOnInvocationError() throws ODataApplicationException, NoSuchMethodException,
      SecurityException, ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionForFilter> c = TestFunctionForFilter.class.getConstructor(JPAODataQueryContext.class);
    final Method m = TestFunctionForFilter.class.getMethod("at2", LocalDate.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("date", m);
    setPrimitiveValue("at2", parameter);
    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, queryContext);

    assertThrows(ODataJPAProcessorException.class, () -> cut.process());
  }

  @Test
  void testRethrowsExceptionOnInvocationTargetError() throws ODataApplicationException, NoSuchMethodException,
      SecurityException, ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionParameter> c = TestFunctionParameter.class.getConstructor(EntityManager.class);
    final Method m = TestFunctionParameter.class.getMethod("sumThrowsException", Integer.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("A", m);
    setPrimitiveValue(Integer.valueOf(5), parameter);
    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("A")).thenReturn(parameter.getMiddle());

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, queryContext);
    assertThrows(ODataApplicationException.class, () -> cut.process());
  }

  @Test
  void testRethrowsExceptionOnParameterError() throws ODataApplicationException, NoSuchMethodException,
      SecurityException, ODataJPAModelException, EdmPrimitiveTypeException {

    final Constructor<TestFunctionForFilter> c = TestFunctionForFilter.class.getConstructor(JPAODataQueryContext.class);
    final Method m = TestFunctionForFilter.class.getMethod("at2", LocalDate.class);
    final Triple<UriParameter, EdmParameter, JPAParameter> parameter = createParameter("date", m);
    setPrimitiveValue(LocalDate.now(), parameter);
    doReturn(c).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriResourceFunction.getParameters()).thenReturn(uriParameters);
    when(edmFunction.getParameter("date")).thenReturn(parameter.getMiddle());

    when(((EdmPrimitiveType) parameter.getMiddle().getType())
        .valueOfString(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(EdmPrimitiveTypeException.class);

    cut = new JPAJavaFunctionProcessor(sd, uriResourceFunction, jpaFunction, queryContext);
    assertThrows(ODataJPADBAdaptorException.class, () -> cut.process());
  }

  private Triple<UriParameter, EdmParameter, JPAParameter> createParameter(final String name, final Method m)
      throws ODataJPAModelException, EdmPrimitiveTypeException {
    final UriParameter uriParameter = mock(UriParameter.class);
    when(uriParameter.getName()).thenReturn(name);
    uriParameters.add(uriParameter);

    final JPAParameter parameter = mock(JPAParameter.class);
    when(parameter.getName()).thenReturn(name);
    doReturn(Short.class).when(parameter).getType();
    when(jpaFunction.getParameter(m.getParameters()[0])).thenReturn(parameter);
    final EdmParameter edmParameter = mock(EdmParameter.class);

    return new ImmutableTriple<>(uriParameter, edmParameter, parameter);
  }

  private void setPrimitiveValue(final Object value, final Triple<UriParameter, EdmParameter, JPAParameter> parameter)
      throws EdmPrimitiveTypeException {
    when(parameter.getLeft().getText()).thenReturn(value == null ? null : value.toString());
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    when(parameter.getMiddle().getType()).thenReturn(edmType);
    when(edmType.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any())).thenReturn(value);
  }

  private void setEnumValue(final Object value, final Triple<UriParameter, EdmParameter, JPAParameter> parameter,
      final boolean enumFound) throws ODataJPAModelException {

    final String ENUM_FQN = "test.FileAccess";
    final FullQualifiedName fqn = new FullQualifiedName(ENUM_FQN);
    final JPAEnumerationAttribute enumAttribute = mock(JPAEnumerationAttribute.class);
    when(parameter.getLeft().getText()).thenReturn(value == null ? null : value.toString());
    final EdmEnumType edmType = mock(EdmEnumType.class);
    when(parameter.getMiddle().getType()).thenReturn(edmType);
    when(parameter.getRight().getTypeFQN()).thenReturn(fqn);
    when(edmType.getKind()).thenReturn(EdmTypeKind.ENUM);
    if (enumFound)
      when(sd.getEnumType(ENUM_FQN)).thenReturn(enumAttribute);
    else
      when(sd.getEnumType(ENUM_FQN)).thenReturn(null);
    when(enumAttribute.enumOf(value.toString())).thenReturn(value);
  }

  private void setComplexValue(final Object value, final Triple<UriParameter, EdmParameter, JPAParameter> parameter)
      throws EdmPrimitiveTypeException {
    when(parameter.getLeft().getText()).thenReturn(value == null ? null : value.toString());
    final EdmComplexType edmType = mock(EdmComplexType.class);
    when(parameter.getMiddle().getType()).thenReturn(edmType);
    when(edmType.getKind()).thenReturn(EdmTypeKind.COMPLEX);
  }
}
