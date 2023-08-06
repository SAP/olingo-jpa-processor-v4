package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public final class JPAFilterExpression implements JPAVisitableExpression {
  private final Literal literal;
  private final BinaryOperatorKind operator;
  private final Member member;

  public JPAFilterExpression(final Member member, final Literal literal, final BinaryOperatorKind operator) {
    super();
    this.literal = literal;
    this.operator = operator;
    this.member = member;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    final T left = visitor.visitMember(member);
    final T right = visitor.visitLiteral(literal);
    return visitor.visitBinaryOperator(operator, left, right);
  }

  @Override
  public UriInfoResource getMember() {
    return member.getResourcePath();
  }

  @Override
  public Literal getLiteral() {
    return literal;
  }

  @Override
  public String toString() {
    return "JPAFilterExpression [literal=" + literal
        + ", operator=" + operator + ", member="
        + "[resourcePath="
        + member.getResourcePath().getUriResourceParts()
        + ", startTypeFilter= " + member.getStartTypeFilter()
        + ", type= " + member.getType()
        + "]]";
  }
}
