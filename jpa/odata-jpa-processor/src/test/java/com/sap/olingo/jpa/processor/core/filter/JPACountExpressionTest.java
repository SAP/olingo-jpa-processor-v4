package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

class JPACountExpressionTest {
  private JPACountExpression cut;

  private Literal literal;
  private BinaryOperatorKind operator;
  private Member member;

  @BeforeEach
  void setup() {
    literal = mock(Literal.class);
    member = mock(Member.class);
  }

  @Test
  void checkGetMember() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    operator = BinaryOperatorKind.EQ;
    cut = new JPACountExpression(member, literal, operator);

    assertEquals(uriInfo, cut.getMember());
  }

  @Test
  void checkIsInvertedDefaultFalse() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    operator = BinaryOperatorKind.EQ;
    cut = new JPACountExpression(member, literal, operator);

    assertFalse(cut.isInversionRequired());
    assertEquals(BinaryOperatorKind.EQ, cut.getOperator());
  }

  @Test
  void checkIsInvertedTrueEqZero() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getType()).thenReturn(EdmInt32.getInstance());
    when(literal.getText()).thenReturn("0");
    operator = BinaryOperatorKind.EQ;
    cut = new JPACountExpression(member, literal, operator);

    assertTrue(cut.isInversionRequired());
    assertEquals(BinaryOperatorKind.NE, cut.getOperator());
  }

  @Test
  void checkIsInvertedTrueLeZero() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getType()).thenReturn(EdmInt32.getInstance());
    when(literal.getText()).thenReturn("0");
    operator = BinaryOperatorKind.LE;
    cut = new JPACountExpression(member, literal, operator);

    assertTrue(cut.isInversionRequired());
    assertEquals(BinaryOperatorKind.NE, cut.getOperator());
  }

  @Test
  void checkIsInvertedFalseGtZero() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getType()).thenReturn(EdmInt32.getInstance());
    when(literal.getText()).thenReturn("0");
    operator = BinaryOperatorKind.GT;
    cut = new JPACountExpression(member, literal, operator);

    assertFalse(cut.isInversionRequired());
    assertEquals(BinaryOperatorKind.GT, cut.getOperator());
  }

  @Test
  void checkAcceptsVisitor() throws ExpressionVisitException, ODataApplicationException {
    @SuppressWarnings("unchecked")
    final ExpressionVisitor<String> visitor = mock(ExpressionVisitor.class);
    when(visitor.visitBinaryOperator(eq(BinaryOperatorKind.GT), anyString(), anyString())).thenReturn("Hello");
    when(visitor.visitLiteral(literal)).thenReturn("0");
    when(visitor.visitMember(member)).thenReturn("Test");
    when(literal.getText()).thenReturn("0");
    operator = BinaryOperatorKind.GT;

    cut = new JPACountExpression(member, literal, operator);
    assertEquals("Hello", cut.accept(visitor));

    verify(visitor).visitMember(member);
    verify(visitor).visitLiteral(literal);
    verify(visitor).visitBinaryOperator(eq(BinaryOperatorKind.GT), anyString(), anyString());
  }

  @Test
  void checkGeZeroNotSupported() {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getType()).thenReturn(EdmInt32.getInstance());
    when(literal.getText()).thenReturn("0");
    operator = BinaryOperatorKind.GE;
    assertThrows(ODataJPAFilterException.class, () -> new JPACountExpression(member, literal, operator));

  }
}
