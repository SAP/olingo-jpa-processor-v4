package org.apache.olingo.jpa.processor.core.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.junit.Before;
import org.junit.Test;

public class TestJPAOperationConverter {
  private CriteriaBuilder cb;

  private JPAOperationConverter converter;
  private Path<Integer> expression;
  private JPAOperationConverter cut;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    converter = mock(JPAOperationConverter.class);
    cb = mock(CriteriaBuilder.class);
    expression = mock(Path.class);
    cut = new JPAOperationConverter(cb);
  }

  @Test
  public void testAddMemberLiteral() throws ODataApplicationException {
    JPAArithmeticOperator operator = mock(JPAArithmeticOperator.class);
    when(operator.getOperator()).thenReturn(BinaryOperatorKind.ADD);
    Expression<?> act = cut.convert(operator);
    act.toString();

  }
}
