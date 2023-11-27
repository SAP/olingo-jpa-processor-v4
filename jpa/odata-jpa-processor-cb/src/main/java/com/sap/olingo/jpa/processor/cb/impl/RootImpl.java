package com.sap.olingo.jpa.processor.cb.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

class RootImpl<X> extends FromImpl<X, X> implements Root<X> {

  RootImpl(final JPAEntityType type, final AliasBuilder aliasBuilder, final CriteriaBuilder cb) {
    super(type, aliasBuilder, cb);
  }

  @Override
  public EntityType<X> getModel() {
    throw new NotImplementedException();
  }
}
