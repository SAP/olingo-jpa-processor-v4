package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

public final class JPAMethodExpression implements JPAVisitableExpression {
  private final MethodKind methodCall;
  private final Member member;
  private final Literal literal;

  public JPAMethodExpression(final Member member, final JPALiteralOperator operand, final MethodKind methodCall) {
    super();
    this.methodCall = methodCall;
    this.member = member;
    this.literal = operand != null ? operand.getLiteral() : null;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    final List<T> parameters = new ArrayList<>(2);
    parameters.add(visitor.visitMember(member));
    if (literal != null)
      parameters.add(visitor.visitLiteral(literal));
    return visitor.visitMethodCall(methodCall, parameters);
  }

  @Override
  public UriInfoResource getMember() {
    return member.getResourcePath();
  }

}
