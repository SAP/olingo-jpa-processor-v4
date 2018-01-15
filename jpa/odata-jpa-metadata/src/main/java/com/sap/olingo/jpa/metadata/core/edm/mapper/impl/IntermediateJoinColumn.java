package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.JoinColumn;

final class IntermediateJoinColumn {
  private String name;
  private String referencedColumnName;

  public IntermediateJoinColumn(final JoinColumn jpaJoinColumn) {
    super();
    this.name = jpaJoinColumn.name();
    this.referencedColumnName = jpaJoinColumn.referencedColumnName();
  }

  public IntermediateJoinColumn(final String name, final String referencedColumnName) {
    super();
    this.name = name;
    this.referencedColumnName = referencedColumnName;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getReferencedColumnName() {
    return referencedColumnName;
  }

  public void setReferencedColumnName(final String referencedColumnName) {
    this.referencedColumnName = referencedColumnName;
  }

}
