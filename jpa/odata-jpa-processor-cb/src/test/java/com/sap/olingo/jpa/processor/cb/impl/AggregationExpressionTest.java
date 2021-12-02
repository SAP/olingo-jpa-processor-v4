package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;

class AggregationExpressionTest extends BuilderBaseTest {
  private ExpressionImpl.AggregationExpression<Long> cut;
  private Path<?> path;
  private JPAEntityType et;
  private From<AdministrativeDivision, AdministrativeDivision> root;
  private AliasBuilder ab;
  private StringBuilder stmt;
  private FromImpl<AdministrativeDivision, AdministrativeDivision> spyRoot;
  private JPAEntityType mockEt;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    et = sd.getEntity(AdministrativeDivision.class);
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    path = root.get("area");
    stmt = new StringBuilder();
    mockEt = mock(JPAEntityType.class);
  }

  @Test
  void testGetExpressionReturnsProvideedExpression() {
    cut = new ExpressionImpl.AggregationExpression<>(SqlAggregation.COUNT, path);
    assertEquals(path, cut.getExpression());
  }

  @Test
  void testAsSQLGeneratesForPath() {
    final String exp = "AVG(E0.\"Area\")";
    cut = new ExpressionImpl.AggregationExpression<>(SqlAggregation.AVG, path);
    assertEquals(exp, cut.asSQL(stmt).toString());
  }

  @Test
  void testAsSQLGeneratesForFrom() {
    final String exp = "COUNT(E0.\"CodePublisher\")";
    // The following is not supported by DB :E0.\"CodePublisher\", E0.\"CodeID\", E0.\"DivisionCode\")";
    cut = new ExpressionImpl.AggregationExpression<>(SqlAggregation.COUNT, root);
    assertEquals(exp, cut.asSQL(stmt).toString());
  }

  @Test
  void testAsSQLReThrowsExceptionOnMissingKey() throws ODataJPAModelException {
    spyRoot = (FromImpl<AdministrativeDivision, AdministrativeDivision>) spy(root);
    spyRoot.st = mockEt;
    cut = new ExpressionImpl.AggregationExpression<>(SqlAggregation.COUNT, spyRoot);
    when(mockEt.getKey()).thenThrow(ODataJPAModelException.class);
    assertThrows(IllegalArgumentException.class, () -> cut.asSQL(stmt));
  }

  @Test
  void testAsSQLReThrowsExceptionOnMissingPath() throws ODataJPAModelException {
    final JPAAttribute mockPath = mock(JPAAttribute.class);
    spyRoot = (FromImpl<AdministrativeDivision, AdministrativeDivision>) spy(root);
    spyRoot.st = mockEt;
    cut = new ExpressionImpl.AggregationExpression<>(SqlAggregation.COUNT, spyRoot);
    when(mockEt.getKey()).thenReturn(Arrays.asList(mockPath));
    when(mockPath.getExternalName()).thenReturn("Test");
    when(mockEt.getPath("Test")).thenThrow(ODataJPAModelException.class);
    assertThrows(IllegalArgumentException.class, () -> cut.asSQL(stmt));
  }
}
