package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testobjects.TestFunctionForFilter;

class JPAJavaFunctionOperatorTest {

  private static final String EXTERNAL_NAME = "At";
  private JPAJavaFunctionOperator cut;
  private JPAVisitor jpaVisitor;
  private JPAJavaFunction jpaFunction;
  private UriResourceFunction resource;
  private JPAServiceDocument sd;
  private CriteriaBuilder cb;
  private From<?, ?> from;
  private List<UriParameter> uriParameters;

  @BeforeEach
  void setUp() throws Exception {
    jpaVisitor = mock(JPAVisitor.class);
    jpaFunction = mock(JPAJavaFunction.class);
    resource = mock(UriResourceFunction.class);
    sd = mock(JPAServiceDocument.class);
    cb = mock(CriteriaBuilder.class);
    from = mock(From.class);
    uriParameters = new ArrayList<>();
    when(jpaVisitor.getSd()).thenReturn(sd);
    when(jpaFunction.getFunctionType()).thenReturn(EdmFunctionType.JavaClass);
    when(jpaFunction.getExternalName()).thenReturn(EXTERNAL_NAME);
    when(jpaVisitor.getCriteriaBuilder()).thenReturn(cb);
    doReturn(from).when(jpaVisitor).getRoot();
    when(resource.getParameters()).thenReturn(uriParameters);
    cut = new JPAJavaFunctionOperator(jpaVisitor, resource, jpaFunction);
  }

  @Test
  void testGetNameReturnsExternalName() {
    assertEquals(EXTERNAL_NAME, cut.getName());
  }

  @Test
  void testGetExecutesFunction() throws NoSuchMethodException, SecurityException, ODataApplicationException,
      ODataJPAModelException, EdmPrimitiveTypeException {

    final LocalDate parameterValue = LocalDate.now();
    final UriParameter uriParameter = mock(UriParameter.class);
    final JPAParameter jpaParameter = mock(JPAParameter.class);
    final EdmParameter edmParameter = mock(EdmParameter.class);
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    final EdmFunction edmFunction = mock(EdmFunction.class);
    final Method m = TestFunctionForFilter.class.getMethod("at", LocalDate.class);
    doReturn(TestFunctionForFilter.class.getConstructor(JPAODataQueryContext.class)).when(jpaFunction).getConstructor();
    doReturn(m).when(jpaFunction).getMethod();

    when(uriParameter.getName()).thenReturn("date");
    when(uriParameter.getText()).thenReturn(parameterValue.toString());
    uriParameters.add(uriParameter);

    when(jpaFunction.getParameter(m.getParameters()[0])).thenReturn(jpaParameter);
    when(jpaParameter.getName()).thenReturn("date");

    when(resource.getFunction()).thenReturn(edmFunction);
    when(edmFunction.getParameter("date")).thenReturn(edmParameter);
    when(edmParameter.getType()).thenReturn(edmType);
    when(edmType.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any())).thenReturn(parameterValue);

    cut.get();
    verify(cb).lessThanOrEqualTo(any(), eq(parameterValue));
  }
}
