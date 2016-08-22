package org.apache.olingo.jpa.processor.core.database;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import org.apache.olingo.jpa.processor.core.filter.JPAAggregationOperation;
import org.apache.olingo.jpa.processor.core.filter.JPAArithmeticOperator;
import org.apache.olingo.jpa.processor.core.filter.JPABooleanOperator;
import org.apache.olingo.jpa.processor.core.filter.JPAComparisonOperator;
import org.apache.olingo.jpa.processor.core.filter.JPAFunctionCall;
import org.apache.olingo.jpa.processor.core.filter.JPAUnaryBooleanOperator;
import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAODataDatabaseOperations {

  public void setCriterialBuilder(final CriteriaBuilder cb);

  public <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException;

  public Expression<Boolean> convert(final JPABooleanOperator jpaOperator) throws ODataApplicationException;

  public Expression<Boolean> convert(final JPAComparisonOperator<?> jpaOperator) throws ODataApplicationException;

  public Object convert(final JPAFunctionCall jpaFunction) throws ODataApplicationException;

  public Expression<Boolean> convert(final JPAUnaryBooleanOperator jpaOperator) throws ODataApplicationException;

  public Expression<Long> convert(final JPAAggregationOperation jpaOperator) throws ODataApplicationException;
}
