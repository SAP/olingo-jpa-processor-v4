package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.Selection;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

class JPAPathWrapper implements JPAPath {

  private final Selection<?> selection;

  public JPAPathWrapper(final Selection<?> sel) {
    this.selection = sel;
  }

  @Override
  public int compareTo(final JPAPath o) {
    return selection.getAlias().compareTo(o.getAlias());
  }

  @Override
  public String getAlias() {
    return selection.getAlias();
  }

  @Override
  public String getDBFieldName() {
    return null;
  }

  @Override
  public JPAAttribute getLeaf() {
    return new JPAAttributeWrapper(selection);
  }

  @Override
  public List<JPAElement> getPath() {
    return Collections.singletonList(getLeaf());
  }

  @Override
  public boolean ignore() {
    return false;
  }

  @Override
  public boolean isPartOfGroups(final List<String> groups) {
    return false;
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public String getPathAsString() {
    return selection.getAlias();
  }
}