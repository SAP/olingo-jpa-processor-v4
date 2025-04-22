package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

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
  private final List<Literal> literals;

  public JPAFilterExpression(final Member member, final Literal literal, final BinaryOperatorKind operator) {
    super();
    this.literal = literal;
    this.operator = operator;
    this.member = member;
    this.literals = List.of();
  }

  public JPAFilterExpression(final Member member, final List<Literal> literals, final BinaryOperatorKind operator) {
    super();
    this.literal = null;
    this.operator = operator;
    this.member = member;
    this.literals = literals;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {

    final T left = visitor.visitMember(member);
    if (literal != null) {
      final T right = visitor.visitLiteral(literal);
      return visitor.visitBinaryOperator(operator, left, right);
    } else {
      final List<T> right = new ArrayList<>(literals.size());
      for (final Literal l : literals)
        right.add(visitor.visitLiteral(l));
      return visitor.visitBinaryOperator(operator, left, right);
    }

  }

//  @Override
//  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
//    T localLeft = this.left.accept(visitor);
//    if (this.right != null) {
//      T localRight = this.right.accept(visitor);
//      return visitor.visitBinaryOperator(operator, localLeft, localRight);
//    } else if (this.expressions != null) {
//      List<T> expressions = new ArrayList<>();
//      for (final Expression expression : this.expressions) {
//        expressions.add(expression.accept(visitor));
//      }
//      return visitor.visitBinaryOperator(operator, localLeft, expressions);
//    }
//    return null;
//  }

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
