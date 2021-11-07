package com.sap.olingo.jpa.processor.core.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

public abstract class JPA_XXX_DatabaseProcessorTest {

  protected JPAODataDatabaseProcessor cut;
  protected EntityManager em;
  protected UriResourceEntitySet uriEntitySet;
  protected JPADataBaseFunction jpaFunction;
  protected UriResourceFunction uriFunction;
  protected EdmEntityType edmEntityType;
  protected EdmFunction edmFunction;
  protected EdmParameter edmElement;
  protected List<UriResource> uriResourceParts;
  protected List<UriParameter> uriParameters;
  protected JPAOperationResultParameter returnParameter;
  protected List<JPAParameter> parameterList;
  protected JPAParameter firstParameter;
  protected UriParameter firstUriParameter;
  protected Query functionQuery;
  protected String oneParameterResult;
  protected String twoParameterResult;
  protected String countResult;

  void initEach() {
    em = mock(EntityManager.class);
    functionQuery = mock(Query.class);
    uriResourceParts = new ArrayList<>();
    uriFunction = mock(UriResourceFunction.class);
    uriEntitySet = mock(UriResourceEntitySet.class);
    edmFunction = mock(EdmFunction.class);
    edmElement = mock(EdmParameter.class);
    edmEntityType = mock(EdmEntityType.class);
    uriResourceParts.add(uriFunction);
    uriParameters = new ArrayList<>();
    firstUriParameter = mock(UriParameter.class);

    jpaFunction = mock(JPADataBaseFunction.class);
    returnParameter = mock(JPAOperationResultParameter.class);
    parameterList = new ArrayList<>();
    firstParameter = mock(JPAParameter.class);

    when(em.createNativeQuery(any(), eq(BusinessPartner.class))).thenReturn(functionQuery);
    when(em.createNativeQuery(any())).thenReturn(functionQuery);
    when(uriEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(uriEntitySet.getKind()).thenReturn(UriResourceKind.entitySet);
    when(uriEntitySet.getKeyPredicates()).thenReturn(uriParameters);
    when(uriFunction.getParameters()).thenReturn(uriParameters);
    when(jpaFunction.getResultParameter()).thenReturn(returnParameter);
    when(uriFunction.getFunction()).thenReturn(edmFunction);
    when(uriFunction.getKind()).thenReturn(UriResourceKind.function);
    when(edmFunction.getParameter(firstParameter.getName())).thenReturn(edmElement);

  }

  @Test
  void testAbortsOnNotImplementedChaining() throws ODataJPAModelException {

    createFunctionWithOneParameter();
    final UriResourceCount uriResourceCount = mock(UriResourceCount.class);
    uriResourceParts.add(uriResourceCount);
    when(uriResourceCount.getKind()).thenReturn(UriResourceKind.value);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));

    assertEquals(act.getStatusCode(), HttpStatusCode.NOT_IMPLEMENTED.getStatusCode());
  }

  @Test
  void testBoundConvertsExceptionOnParameterProblem() throws ODataJPAModelException {

    createBoundFunctionWithOneParameter();
    when(jpaFunction.getParameter()).thenThrow(ODataJPAModelException.class);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testBoundFunctionWithOneParameterCount() throws ODataApplicationException,
      ODataJPAModelException {
    createBoundFunctionWithOneParameter();

    final UriResourceCount uriResourceCount = mock(UriResourceCount.class);
    uriResourceParts.add(uriResourceCount);
    when(uriResourceCount.getKind()).thenReturn(UriResourceKind.count);
    when(functionQuery.getSingleResult()).thenReturn(5L);

    final List<Long> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);

    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(countResult)));
    verify(functionQuery, times(1)).setParameter(1, "5");
    verify(functionQuery, times(0)).getResultList();
    verify(functionQuery, times(1)).getSingleResult();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(5L, act.get(0));
  }

  @Test
  void testBoundFunctionWithOneParameterReturnsBuPas() throws ODataApplicationException,
      ODataJPAModelException {

    createBoundFunctionWithOneParameter();
    final List<BusinessPartner> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);

    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(oneParameterResult)), eq(
        BusinessPartner.class));
    verify(functionQuery, times(1)).setParameter(1, "5");
    assertNotNull(act);
    assertEquals(2, act.size());
  }

  @Test
  void testBoundFunctionWithTwoParameterReturnsBuPas() throws ODataApplicationException,
      ODataJPAModelException {

    createBoundFunctionWithOneParameter();
    addSecondBoundParameter();
    final List<BusinessPartner> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);
    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(twoParameterResult)), eq(
        BusinessPartner.class));
    verify(functionQuery, times(1)).setParameter(1, "5");
    verify(functionQuery, times(1)).setParameter(2, "3");
    assertNotNull(act);
    assertEquals(2, act.size());
  }

  @Test
  void testBoundRaisesExceptionOnMissingParameter() throws ODataJPAModelException {

    createBoundFunctionWithOneParameter();
    when(uriEntitySet.getKeyPredicates()).thenReturn(new ArrayList<>());

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));

    assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testCheckRaiseExceptionOnProblemValueToString() throws ODataJPAModelException,
      EdmPrimitiveTypeException {

    createBoundFunctionWithOneParameter();
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    when(edmElement.getType()).thenReturn(edmType);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(EdmPrimitiveTypeException.class);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));

    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());

  }

  @Test
  void testCheckRaisesExceptionOnIsBound() throws ODataJPAModelException {

    createBoundFunctionWithOneParameter();
    when(jpaFunction.isBound()).thenThrow(ODataJPAModelException.class);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testUnboundConvertsExceptionOnParameterProblem() throws ODataJPAModelException {

    createFunctionWithOneParameter();
    when(jpaFunction.getParameter()).thenThrow(ODataJPAModelException.class);

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));
    assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testUnboundFunctionWithOneParameterCount() throws ODataApplicationException, ODataJPAModelException {

    createFunctionWithOneParameter();

    final UriResourceCount uriResourceCount = mock(UriResourceCount.class);
    uriResourceParts.add(uriResourceCount);
    when(uriResourceCount.getKind()).thenReturn(UriResourceKind.count);
    when(functionQuery.getSingleResult()).thenReturn(5L);

    final List<Long> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);

    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(countResult)));
    verify(functionQuery, times(1)).setParameter(1, "5");
    verify(functionQuery, times(0)).getResultList();
    verify(functionQuery, times(1)).getSingleResult();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(5L, act.get(0));
  }

  @Test
  void testUnboundFunctionWithOneParameterReturnsBuPas() throws ODataApplicationException,
      ODataJPAModelException {

    createFunctionWithOneParameter();

    final List<BusinessPartner> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);
    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(oneParameterResult)), eq(
        BusinessPartner.class));
    verify(functionQuery, times(1)).setParameter(1, "5");
    assertNotNull(act);
    assertEquals(2, act.size());
  }

  @Test
  void testUnboundFunctionWithTwoParameterReturnsBuPas() throws ODataApplicationException,
      ODataJPAModelException {

    createFunctionWithOneParameter();
    addSecondParameter();

    final List<BusinessPartner> act = cut.executeFunctionQuery(uriResourceParts, jpaFunction, em);
    verify(em, times(1)).createNativeQuery((String) argThat(new Equals(twoParameterResult)), eq(
        BusinessPartner.class));
    verify(functionQuery, times(1)).setParameter(1, "5");
    verify(functionQuery, times(1)).setParameter(2, "3");
    assertNotNull(act);
    assertEquals(2, act.size());
  }

  @Test
  void testUnboundRaisesExceptionOnMissingParameter() throws ODataJPAModelException {

    createFunctionWithOneParameter();
    when(uriFunction.getParameters()).thenReturn(new ArrayList<>());

    final ODataApplicationException act = assertThrows(ODataApplicationException.class,
        () -> cut.executeFunctionQuery(uriResourceParts, jpaFunction, em));
    assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), act.getStatusCode());

  }

  protected void addSecondBoundParameter() {
    final JPAParameter secondParameter = mock(JPAParameter.class);
    final UriParameter secondUriParameter = mock(UriParameter.class);
    final EdmParameter edmSecondElement = mock(EdmParameter.class);

    parameterList.add(secondParameter);
    uriParameters.add(secondUriParameter);
    when(secondUriParameter.getText()).thenReturn("3");
    when(secondParameter.getName()).thenReturn("B");
    when(secondUriParameter.getName()).thenReturn("B");
    when(edmEntityType.getProperty("B")).thenReturn(edmSecondElement);
    when(edmSecondElement.getType()).thenReturn(EdmString.getInstance());
    when(secondParameter.getMaxLength()).thenReturn(10);
    when(secondParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
  }

  protected void addSecondParameter() {
    final JPAParameter secondParameter = mock(JPAParameter.class);
    final UriParameter secondUriParameter = mock(UriParameter.class);
    final EdmParameter edmSecondElement = mock(EdmParameter.class);

    parameterList.add(secondParameter);
    uriParameters.add(secondUriParameter);
    when(secondUriParameter.getText()).thenReturn("3");
    when(secondParameter.getName()).thenReturn("B");
    when(secondUriParameter.getName()).thenReturn("B");
    when(edmFunction.getParameter("B")).thenReturn(edmSecondElement);
    when(edmSecondElement.getType()).thenReturn(EdmString.getInstance());
    when(secondParameter.getMaxLength()).thenReturn(10);
    when(secondParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
  }

  protected void createBoundFunctionWithOneParameter() throws ODataJPAModelException {

    uriResourceParts.add(0, uriEntitySet);
    when(uriFunction.getParameters()).thenReturn(new ArrayList<>());
    when(jpaFunction.isBound()).thenReturn(Boolean.TRUE);
    when(returnParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });

    when(jpaFunction.getDBName()).thenReturn("Example");
    when(jpaFunction.getParameter()).thenReturn(parameterList);

    parameterList.add(firstParameter);
    when(firstParameter.getName()).thenReturn("A");

    uriParameters.add(firstUriParameter);
    when(firstUriParameter.getName()).thenReturn("A");
    when(edmEntityType.getProperty("A")).thenReturn(edmElement);
    when(firstUriParameter.getText()).thenReturn("5");
    when(edmElement.getType()).thenReturn(EdmString.getInstance());
    when(firstParameter.getMaxLength()).thenReturn(10);
    when(firstParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });

    when(functionQuery.getResultList()).thenReturn(Arrays.asList(new Organization(), new Organization()));
  }

  protected void createFunctionWithOneParameter() throws ODataJPAModelException {
    when(returnParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return BusinessPartner.class;
      }
    });
    when(jpaFunction.getDBName()).thenReturn("Example");
    when(jpaFunction.getParameter()).thenReturn(parameterList);

    parameterList.add(firstParameter);
    when(firstParameter.getName()).thenReturn("A");

    uriParameters.add(firstUriParameter);
    when(firstUriParameter.getName()).thenReturn("A");
    when(edmFunction.getParameter("A")).thenReturn(edmElement);
    when(firstUriParameter.getText()).thenReturn("5");
    when(edmElement.getType()).thenReturn(EdmString.getInstance());
    when(firstParameter.getMaxLength()).thenReturn(10);
    when(firstParameter.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });

    when(functionQuery.getResultList()).thenReturn(Arrays.asList(new Organization(), new Organization()));
  }

}
