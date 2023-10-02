package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

class JPANullExpressionTest {
  private JPANullExpression cut;

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
    cut = new JPANullExpression(member, literal, operator);

    assertEquals(uriInfo, cut.getMember());
  }

  @Test
  void checkAcceptReturnsNull() throws ExpressionVisitException, ODataApplicationException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    operator = BinaryOperatorKind.EQ;
    cut = new JPANullExpression(member, literal, operator);

    assertNull(cut.accept(null));
  }

  @Test
  void checkIsInvertedDefaultFalse() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getText()).thenReturn("null");
    operator = BinaryOperatorKind.NE;
    cut = new JPANullExpression(member, literal, operator);

    assertFalse(cut.isInversionRequired());
  }

  @Test
  void checkIsInvertedTrueNeZero() throws ODataJPAFilterException {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    when(member.getResourcePath()).thenReturn(uriInfo);
    when(literal.getText()).thenReturn("null");
    operator = BinaryOperatorKind.EQ;
    cut = new JPANullExpression(member, literal, operator);

    assertTrue(cut.isInversionRequired());
  }
}
