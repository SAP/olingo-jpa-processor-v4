package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAEmptyDebugger;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;

class JPAJoinCountQueryTest extends TestQueryBase {
  private CriteriaBuilder cb;
  @SuppressWarnings("rawtypes")
  private CriteriaQuery cq;
  private EntityManager em;
  private JPAODataRequestContextAccess localContext;
  private JPAHttpHeaderMap headerMap;
  private JPARequestParameterMap parameterMap;

  @SuppressWarnings("unchecked")
  @Override
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    em = mock(EntityManager.class);
    cb = spy(emf.getCriteriaBuilder());
    cq = mock(CriteriaQuery.class);
    localContext = mock(JPAODataRequestContextAccess.class);
    headerMap = mock(JPAHttpHeaderMap.class);
    parameterMap = mock(JPARequestParameterMap.class);

    buildUriInfo("BusinessPartners", "BusinessPartner");
    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType(BusinessPartner.class);
    createHeaders();

    when(localContext.getUriInfo()).thenReturn(uriInfo);
    when(localContext.getEntityManager()).thenReturn(em);
    when(localContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(localContext.getDebugger()).thenReturn(new JPAEmptyDebugger());
    when(localContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    when(localContext.getHeader()).thenReturn(headerMap);
    when(localContext.getRequestParameter()).thenReturn(parameterMap);
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(any())).thenReturn(cq);
    when(cb.createTupleQuery()).thenReturn(cq);

    cut = new JPAJoinCountQuery(null, localContext);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCountResultsIsLong() throws ODataApplicationException {
    final TypedQuery<Tuple> typedQuery = mock(TypedQuery.class);
    final Expression<Long> countExpression = mock(Expression.class);
    final var result = mock(Tuple.class);
    when(cq.multiselect(any(), any())).thenReturn(cq);
    doReturn(countExpression).when(cb).countDistinct(any());
    doReturn(countExpression).when(cb).count(any());
    when(em.createQuery(any(CriteriaQuery.class))).thenReturn(typedQuery);
    when(result.get(0)).thenReturn(5L);
    when(typedQuery.getSingleResult()).thenReturn(result);
    final var act = ((JPAJoinCountQuery) cut).countResults();
    assertEquals(5L, act);
  }
}
