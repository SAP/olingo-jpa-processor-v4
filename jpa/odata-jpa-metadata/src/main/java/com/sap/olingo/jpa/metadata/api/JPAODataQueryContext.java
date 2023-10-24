package com.sap.olingo.jpa.metadata.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;

public interface JPAODataQueryContext {

  CriteriaBuilder getCriteriaBuilder();

  <X, Y> From<X, Y> getFrom();

  JPAEntityType getEntityType();
}
