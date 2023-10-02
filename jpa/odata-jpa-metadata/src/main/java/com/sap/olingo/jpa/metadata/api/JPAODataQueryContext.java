package com.sap.olingo.jpa.metadata.api;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPAODataQueryContext {

  CriteriaBuilder getCriteriaBuilder();

  <X, Y> From<X, Y> getFrom();

  JPAEntityType getEntityType();
}
