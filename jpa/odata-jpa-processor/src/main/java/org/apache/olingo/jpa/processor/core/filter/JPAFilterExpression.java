package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

class JPAFilterExpression implements VisitableExpression {
  private UriInfoResource member;
  private Literal literal;
  private BinaryOperatorKind operator;

  public JPAFilterExpression(final UriInfoResource member, final Literal literal, final BinaryOperatorKind operator) {
    super();
    this.member = member;
    this.literal = literal;
    this.operator = operator;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    final T left = visitor.visitMember(member);
    final T right = visitor.visitLiteral(literal);
    return visitor.visitBinaryOperator(operator, left, right);
  }

}
