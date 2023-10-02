package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.time.LocalDate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class ExampleFunctionForFilter implements ODataFunction {
  private final JPAODataQueryContext stmt;

  public ExampleFunctionForFilter(final JPAODataQueryContext stmt) {
    super();
    this.stmt = stmt;
  }

  @EdmFunction(returnType = @ReturnType(type = Boolean.class), hasFunctionImport = false, isBound = false)
  public Object at(@EdmParameter(name = "date") final LocalDate date) {
    final CriteriaBuilder cb = stmt.getCriteriaBuilder();
    final From<?, ?> from = stmt.getFrom();
    return cb.and(
        cb.lessThanOrEqualTo(from.get("validityStartDate"), date),
        cb.greaterThanOrEqualTo(from.get("validityEndDate"), date));
  }
}
