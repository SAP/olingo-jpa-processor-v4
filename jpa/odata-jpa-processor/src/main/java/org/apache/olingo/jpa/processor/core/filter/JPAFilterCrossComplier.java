package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

/**
 * Cross compiles Olingo generated AST of an OData filter into JPA criteria builder where condition.
 * 
 * Details can be found:
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301"
 * >OData Version 4.0 Part 1 - 11.2.5.1 System Query Option $filter </a>
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094"
 * >OData Version 4.0 Part 2 - 5.1.1 System Query Option $filter</a>
 * <a href=
 * "https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt">
 * odata-abnf-construction-rules</a>
 * @author Oliver Grande
 *
 */
public class JPAFilterCrossComplier {
  private final JPAOperationConverter converter;
  private final FilterOption filterTree;
  // TODO Check if it is allowed to select via navigation
  // ...Organizations?$select=Roles/RoleCategory eq 'C'
  private final Root<?> root;
  private final JPAEntityType jpaEntityType;

  public JPAFilterCrossComplier(final JPAEntityType jpaEntityType, final Root<?> root,
      final JPAOperationConverter converter,
      final FilterOption filterTree) {
    super();
    this.converter = converter;
    this.filterTree = filterTree;
    this.root = root;
    this.jpaEntityType = jpaEntityType;
  }

  @SuppressWarnings("unchecked")
  public Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException {
    ExpressionVisitor<JPAOperator> v = new visitor();

    org.apache.olingo.server.api.uri.queryoption.expression.Expression e = filterTree.getExpression();
    filterTree.getKind();
    return (Expression<Boolean>) e.accept(v).get();
  }

  private class visitor implements ExpressionVisitor<JPAOperator> {
    int count = 0;

    @SuppressWarnings("rawtypes")
    @Override
    public JPAOperator visitBinaryOperator(BinaryOperatorKind operator, JPAOperator left, JPAOperator right)
        throws ExpressionVisitException, ODataApplicationException {
      count += 1;
      System.out.println("Count: " + count + " Binary " + operator);
      if (operator == BinaryOperatorKind.EQ
          || operator == BinaryOperatorKind.NE
          || operator == BinaryOperatorKind.GE
          || operator == BinaryOperatorKind.GT
          || operator == BinaryOperatorKind.LT
          || operator == BinaryOperatorKind.LE)
        return new JPAComparisonOperator(converter, operator, left, right);
      else if (operator == BinaryOperatorKind.AND || operator == BinaryOperatorKind.OR)
        return new JPABooleanOperator(converter, operator, (JPAExpressionOperator) left, (JPAExpressionOperator) right);
      else
        throw new ODataApplicationException("Unsupported Operator" + operator, HttpStatusCode.BAD_REQUEST
            .getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public JPAOperator visitUnaryOperator(UnaryOperatorKind operator, JPAOperator operand)
        throws ExpressionVisitException, ODataApplicationException {
      count += 1;
      System.out.println("Count: " + count + " Unary " + operator);
      if (operator == UnaryOperatorKind.NOT)
        return new JPAUnaryBooleanOperator(converter, operator, (JPAExpressionOperator) operand);
      else
        throw new ODataApplicationException("Unsupported Operator" + operator, HttpStatusCode.BAD_REQUEST
            .getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public JPAOperator visitMethodCall(MethodKind methodCall, List<JPAOperator> parameters)
        throws ExpressionVisitException,
        ODataApplicationException {
      count += 1;
      System.out.println("Count: " + count + " Method " + methodCall.name());
      return null;
    }

    @Override
    public JPAOperator visitLambdaExpression(String lambdaFunction, String lambdaVariable,
        org.apache.olingo.server.api.uri.queryoption.expression.Expression expression) throws ExpressionVisitException,
            ODataApplicationException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public JPAOperator visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
      count += 1;
      System.out.println("Count: " + count + " Literal " + literal.getText());
      return new JPALiteralOperator(literal);
    }

    @Override
    public JPAOperator visitMember(UriInfoResource member) throws ExpressionVisitException, ODataApplicationException {
      count += 1;
      System.out.println("Count: " + count + " Member " + member.getUriResourceParts().get(0).getKind());
      return new JPAMemberOperator(jpaEntityType, root, member);
    }

    @Override
    public JPAOperator visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public JPAOperator visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public JPAOperator visitLambdaReference(String variableName) throws ExpressionVisitException,
        ODataApplicationException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public JPAOperator visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException,
        ODataApplicationException {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
