package com.sap.olingo.jpa.processor.core.query;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

class JPAQueryCreationResult {
  private final TypedQuery<Tuple> query;
  private final SelectionPathInfo<JPAPath> selection;

  JPAQueryCreationResult(final TypedQuery<Tuple> query, final SelectionPathInfo<JPAPath> selection) {
    this.query = query;
    this.selection = selection;
  }

  TypedQuery<Tuple> getQuery() {
    return query;
  }

  SelectionPathInfo<JPAPath> getSelection() {
    return selection;
  }

  @Override
  public String toString() {
    return "JPAQueryCreationResult [query=" + query + ", selection=" + selection + "]";
  }

}
