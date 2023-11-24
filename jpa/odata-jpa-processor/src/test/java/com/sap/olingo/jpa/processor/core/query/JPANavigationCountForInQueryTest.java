package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.server.api.ODataApplicationException;
import org.mockito.ArgumentCaptor;

import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;

class JPANavigationCountForInQueryTest extends JPANavigationCountQueryTest {
  @Override
  protected JPANavigationSubQuery createCut() throws ODataApplicationException {
    return new JPANavigationCountForInQuery(odata, helper.sd, edmEntityType, em, parent, from, association, Optional
        .of(claimsProvider), Collections.emptyList());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Subquery<Comparable<?>> createSubQuery() {
    return mock(ProcessorSubquery.class);
  }

  @Override
  protected void assertAggregateClaims(final Path<Object> roleCategoryPath, final Path<Object> idPath,
      final Path<Object> sourceIdPath, final List<Path<Comparable<?>>> paths) {
    assertNotNull(cut);
    verify(cb).equal(roleCategoryPath, "A");
    assertContainsPath(paths, 1, "iD");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void assertMultipleJoinColumns(final Subquery<Comparable<?>> subQuery,
      final List<Path<Comparable<?>>> paths) {
    final ArgumentCaptor<List<Selection<?>>> selectionCaptor = ArgumentCaptor.forClass(List.class);
    final ArgumentCaptor<List<Expression<?>>> groupByCaptor = ArgumentCaptor.forClass(List.class);
    verify((ProcessorSubquery<Comparable<?>>) subQuery).multiselect(selectionCaptor.capture());
    verify((ProcessorSubquery<Comparable<?>>) subQuery).groupBy(groupByCaptor.capture());

    assertContainsPath(selectionCaptor.getValue(), 3, "codePublisher", "parentCodeID", "parentDivisionCode");
    assertContainsPath(groupByCaptor.getValue(), 3, "codePublisher", "parentCodeID", "parentDivisionCode");
    assertContainsPath(paths, 3, "codePublisher", "codeID", "divisionCode");
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void assertOneJoinColumnsWithClaims(final Subquery<Comparable<?>> subQuery, final Predicate equalExpression,
      final List<Path<Comparable<?>>> paths) {
    final ArgumentCaptor<Expression<Comparable<?>>> selectionCaptor = ArgumentCaptor.forClass(Expression.class);
    final ArgumentCaptor<List<Expression<?>>> groupByCaptor = ArgumentCaptor.forClass(List.class);
    final ArgumentCaptor<Expression<Boolean>> restrictionCaptor = ArgumentCaptor.forClass(Expression.class);
    verify(subQuery).select(selectionCaptor.capture());
    verify(subQuery).groupBy(groupByCaptor.capture());
    verify(subQuery).where(restrictionCaptor.capture());

    assertEquals("businessPartnerID", selectionCaptor.getValue().getAlias());
    assertContainsPath(groupByCaptor.getValue(), 1, "businessPartnerID");
    assertEquals(equalExpression, restrictionCaptor.getValue());
    assertContainsPath(paths, 1, "iD");
  }

  @Override
  protected void assertLeftEarlyAccess() {
    assertThrows(ODataJPAIllegalAccessException.class, () -> cut.getLeftPaths());
  }

}
