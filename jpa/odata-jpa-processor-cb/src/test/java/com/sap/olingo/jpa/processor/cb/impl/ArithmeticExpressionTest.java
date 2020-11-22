package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ArithmeticExpression;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;

class ArithmeticExpressionTest extends BuilderBaseTest {
  private ExpressionImpl.ArithmeticExpression<Long> cut;
  private From<AdministrativeDivision, AdministrativeDivision> root;
  private PathImpl<Long> left;
  private PathImpl<Long> right;
  private JPAEntityType et;
  private AliasBuilder ab;
  private StringBuilder stmt;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @BeforeEach
  public void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    et = sd.getEntity(AdministrativeDivision.class);
    root = new FromImpl<>(et, ab, mock(CriteriaBuilder.class));
    left = (PathImpl) root.get("area");
    right = (PathImpl) root.get("population");
    stmt = new StringBuilder();

    cut = new ArithmeticExpression<>(right, left, SqlArithmetic.QUOT);
  }

  @Test
  void testAsSql() {
    final String exp = "(E0.\"Population\" / E0.\"Area\")";
    assertEquals(exp, cut.asSQL(stmt).toString());
  }
}
