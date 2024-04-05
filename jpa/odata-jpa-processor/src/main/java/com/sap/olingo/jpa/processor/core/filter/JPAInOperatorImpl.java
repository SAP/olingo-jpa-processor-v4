package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

public class JPAInOperatorImpl<T, X extends JPAOperator> implements JPAInOperator<T, X> {

  private final JPAOperationConverter converter;
  private final JPAOperator left;
  private final List<X> right;

  public JPAInOperatorImpl(final JPAOperationConverter converter,
      final JPAOperator left, final List<X> right) {
    this.converter = converter;
    this.left = left;
    this.right = right;
  }

  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  @Override
  public String getName() {
    return getOperator().name();
  }

  @Override
  public BinaryOperatorKind getOperator() {
    return BinaryOperatorKind.IN;
  }

  @Override
  public List<X> getFixValues() {
    return right;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Expression<T> getLeft() throws ODataApplicationException {
    return (Expression<T>) left.get();
  }

}
