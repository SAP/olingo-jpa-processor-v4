package com.sap.olingo.jpa.processor.core.query;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public record JPAQueryCreationResult(TypedQuery<Tuple> query, SelectionPathInfo<JPAPath> selection) {

  @Override
  public String toString() {
    return "JPAQueryCreationResult [query=" + query + ", selection=" + selection + "]";
  }

}
