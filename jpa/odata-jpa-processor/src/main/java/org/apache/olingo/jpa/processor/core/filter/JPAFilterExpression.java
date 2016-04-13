package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public class JPAFilterExpression implements VisitableExpression {
  // private UriInfoResource member;
  private Literal literal;
  private BinaryOperatorKind operator;
  private final Member member;

  // Olingo 4.1.0
//  public JPAFilterExpression(final UriInfoResource member, final Literal literal, final BinaryOperatorKind operator) {
//    super();
//    this.member = member;
//    this.literal = literal;
//    this.operator = operator;
//    this.member2 = null;
//  }

  // Olingo 4.2.0
  public JPAFilterExpression(final Member member, final Literal literal, final BinaryOperatorKind operator) {
    super();
    // this.member = null;
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

  public UriInfoResource getMember() {
    return member.getResourcePath();
  }
}
