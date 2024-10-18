package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

class JPAExpandQueryFactoryTest {
  private JPAExpandQueryFactory cut;
  private JPAODataRequestContextAccess requestContext;
  private OData odata;
  private JPAExpandItemInfo item;
  private EntityManager em;
  private JPAEdmProvider edmProvider;
  private CriteriaBuilder cb;
  private JPAAssociationPath associationPath;

  @BeforeEach
  void setup() throws ODataJPAProcessorException {
    final var navigationInfo = mock(JPANavigationPropertyInfo.class);

    requestContext = mock(JPAODataRequestContextAccess.class);
    odata = OData.newInstance();
    item = mock(JPAExpandItemInfo.class);
    em = mock(EntityManager.class);
    edmProvider = mock(JPAEdmProvider.class);
    cb = mock(CriteriaBuilder.class);
    associationPath = mock(JPAAssociationPath.class);

    when(requestContext.getGroupsProvider()).thenReturn(Optional.empty());
    when(requestContext.getClaimsProvider()).thenReturn(Optional.empty());
    when(requestContext.getDatabaseProcessor()).thenReturn(null);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(navigationInfo.getAssociationPath()).thenReturn(associationPath);

    when(item.getHops()).thenReturn(Arrays.asList(navigationInfo, navigationInfo));

  }

  @Test
  void testExpandSubQueryProvidedForProcessor() throws ODataException {
    final var processorCb = mock(ProcessorCriteriaBuilder.class);
    cut = new JPAExpandQueryFactory(odata, requestContext, processorCb);
    assertTrue(cut.createQuery(item, Optional.empty()) instanceof JPAExpandSubQuery);
  }

  @Test
  void testExpandJoinQueryProvidedForProcessor() throws ODataException {
    cut = new JPAExpandQueryFactory(odata, requestContext, cb);
    assertTrue(cut.createQuery(item, Optional.empty()) instanceof JPAExpandJoinQuery);
  }

  @Test
  void testExpandSubCountQueryProvidedForProcessor() throws ODataException {
    final var processorCb = mock(ProcessorCriteriaBuilder.class);
    cut = new JPAExpandQueryFactory(odata, requestContext, processorCb);
    assertTrue(cut.createCountQuery(item, Optional.empty()) instanceof JPAExpandSubCountQuery);
  }

  @Test
  void testExpandJoinCountQueryProvidedForProcessor() throws ODataException {
    cut = new JPAExpandQueryFactory(odata, requestContext, cb);
    assertTrue(cut.createCountQuery(item, Optional.empty()) instanceof JPAExpandJoinCountQuery);
  }
}
