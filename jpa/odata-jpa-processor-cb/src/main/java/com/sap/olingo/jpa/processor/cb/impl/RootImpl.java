package com.sap.olingo.jpa.processor.cb.impl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

class RootImpl<X> extends FromImpl<X, X> implements Root<X> {

  RootImpl(final JPAEntityType type, final AliasBuilder ab, final CriteriaBuilder cb) {
    super(type, ab, cb);
  }

  @Override
  public EntityType<X> getModel() {
    throw new NotImplementedException();
  }
}
