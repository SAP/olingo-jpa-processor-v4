package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import jakarta.persistence.JoinColumn;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;

final class IntermediateJoinColumn implements JPAJoinColumn {
  private String name;
  private String referencedColumnName;

  IntermediateJoinColumn(final JoinColumn jpaJoinColumn) {
    super();
    this.name = jpaJoinColumn.name();
    this.referencedColumnName = jpaJoinColumn.referencedColumnName();
  }

  IntermediateJoinColumn(final String name, final String referencedColumnName) {
    super();
    this.name = name;
    this.referencedColumnName = referencedColumnName;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getReferencedColumnName() {
    return referencedColumnName;
  }

  public void setReferencedColumnName(final String referencedColumnName) {
    this.referencedColumnName = referencedColumnName;
  }

  @Override
  public String toString() {
    return "IntermediateJoinColumn [name=" + name + ", referencedColumnName=" + referencedColumnName + "]";
  }

}
