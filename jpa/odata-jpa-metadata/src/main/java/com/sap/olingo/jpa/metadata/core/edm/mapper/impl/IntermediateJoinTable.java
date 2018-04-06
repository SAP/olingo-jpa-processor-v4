package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateJoinTable implements JPAJoinTable {
  private final IntermediateNavigationProperty intermediateProperty;
  private final JoinTable jpaJoinTable;
  private final IntermediateStructuredType sourceType;
  private List<IntermediateJoinColumn> joinColumns = null;
  private List<IntermediateJoinColumn> inverseJoinColumns = null;
  private JPAEntityType jpaEntityType;

  IntermediateJoinTable(final IntermediateNavigationProperty intermediateProperty, final JoinTable jpaJoinTable,
      final IntermediateSchema schema) {
    super();
    this.intermediateProperty = intermediateProperty;
    this.jpaJoinTable = jpaJoinTable;
    this.sourceType = intermediateProperty.getSourceType();
    this.jpaEntityType = schema.getEntityType(jpaJoinTable.catalog(), jpaJoinTable.schema(), jpaJoinTable.name());
  }

  private IntermediateJoinTable(final IntermediateJoinTable intermediateJoinTable,
      final IntermediateNavigationProperty mappedBy) throws ODataJPAModelException {

    this.jpaJoinTable = intermediateJoinTable.jpaJoinTable;
    this.sourceType = intermediateJoinTable.getTargetType();
    this.jpaEntityType = intermediateJoinTable.jpaEntityType;
    this.intermediateProperty = mappedBy;
    this.joinColumns = intermediateJoinTable.buildInverseJoinColumns();
    this.inverseJoinColumns = intermediateJoinTable.buildJoinColumns();
  }

  @Override
  public String getAlias(String dbFieldName) {
    for (IntermediateJoinColumn column : joinColumns) {
      if (column.getName().equals(dbFieldName))
        return column.getReferencedColumnName();
    }
    return null;
  }

  @Override
  public String getInverseAlias(String dbFieldName) {
    try {
      buildInverseJoinColumns();
    } catch (ODataJPAModelException e) {
      throw new IllegalArgumentException(e);
    }
    for (IntermediateJoinColumn column : inverseJoinColumns) {
      if (column.getName().equals(dbFieldName))
        return column.getReferencedColumnName();
    }
    return null;
  }

  /**
   * Returns the name of database table
   * @return
   */
  @Override
  public String getTableName() {
    return jpaJoinTable.name();
  }

  private IntermediateStructuredType getTargetType() throws ODataJPAModelException {
    return (IntermediateStructuredType) intermediateProperty.getTargetEntity();
  }

  List<IntermediateJoinColumn> buildInverseJoinColumns()
      throws ODataJPAModelException {

    if (inverseJoinColumns == null) {
      inverseJoinColumns = new ArrayList<>(jpaJoinTable.inverseJoinColumns().length);
      for (JoinColumn column : jpaJoinTable.inverseJoinColumns()) {
        if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty())
          if (jpaJoinTable.joinColumns().length > 1)
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
                intermediateProperty.getInternalName());
          else
            inverseJoinColumns.add(new IntermediateJoinColumn(
                ((IntermediateProperty) ((IntermediateEntityType) getTargetType()).getKey().get(0)).getDBFieldName(),
                column.name()));
        else
          inverseJoinColumns.add(new IntermediateJoinColumn(column.referencedColumnName(), column.name()));
      }
    }
    return inverseJoinColumns;
  }

  List<IntermediateJoinColumn> buildJoinColumns() throws ODataJPAModelException {
    if (joinColumns == null) {
      joinColumns = new ArrayList<>(jpaJoinTable.inverseJoinColumns().length);

      for (JoinColumn column : jpaJoinTable.joinColumns()) {
        if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty())
          if (jpaJoinTable.joinColumns().length > 1)
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
                intermediateProperty.getInternalName());
          else if (!(intermediateProperty.getSourceType() instanceof IntermediateEntityType))
            throw new ODataJPAModelException(
                ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS_COMPEX,
                intermediateProperty.getInternalName());
          else {
            joinColumns.add(new IntermediateJoinColumn(
                ((IntermediateProperty) ((IntermediateEntityType) intermediateProperty.getSourceType()).getKey().get(0))
                    .getDBFieldName(), column.name()));
          }
        else
          joinColumns.add(new IntermediateJoinColumn(column.referencedColumnName(), column.name()));
      }
    }
    return joinColumns;
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  @Override
  public List<JPAOnConditionItem> getJoinColumns() throws ODataJPAModelException {
    assert jpaEntityType != null;
    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (IntermediateJoinColumn column : joinColumns) {
      result.add(new JPAOnConditionItemImpl(
          sourceType.getPathByDBField(column.getName()),
          ((IntermediateEntityType) jpaEntityType).getPathByDBField(column.getReferencedColumnName())));

    }
    return result;
  }

  @Override
  public List<JPAOnConditionItem> getInversJoinColumns() throws ODataJPAModelException {
    assert jpaEntityType != null;
    final IntermediateStructuredType targetType = (IntermediateStructuredType) intermediateProperty.getTargetEntity();
    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (IntermediateJoinColumn column : inverseJoinColumns) {
      result.add(new JPAOnConditionItemImpl(
          ((IntermediateEntityType) jpaEntityType).getPathByDBField(column.getReferencedColumnName()),
          targetType.getPathByDBField(column.getName())));

    }
    return result;
  }

  IntermediateJoinTable asMapped(final IntermediateNavigationProperty mappedBy) throws ODataJPAModelException {
    return new IntermediateJoinTable(this, mappedBy);
  }
}