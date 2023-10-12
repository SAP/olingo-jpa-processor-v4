package com.sap.olingo.jpa.processor.core.filter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;

import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public class ODataJPAQueryContext implements JPAODataQueryContext {

  private final From<?, ?> from;
  private final CriteriaBuilder cb;
  private final JPAEntityType et;

  public ODataJPAQueryContext(final JPAVisitor visitor) {
    this.from = visitor.getRoot();
    this.cb = visitor.getCriteriaBuilder();
    this.et = visitor.getEntityType();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X, Y> From<X, Y> getFrom() {
    return (From<X, Y>) from;
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    return cb;
  }
}
