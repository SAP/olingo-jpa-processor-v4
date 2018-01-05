package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateJoinTable implements JPAJoinTable {
  private final IntermediateNavigationProperty intermediateProperty;
  private final JoinTable jpaJoinTable;
  private List<IntermediateJoinColumn> joinColumns = null;
  private List<IntermediateJoinColumn> inverseJoinColumns = null;
  private IntermediateNavigationProperty intermediateMappedProperty = null;

  IntermediateJoinTable(IntermediateNavigationProperty intermediateProperty, JoinTable jpaJoinTable) {
    super();
    this.intermediateProperty = intermediateProperty;
    this.jpaJoinTable = jpaJoinTable;
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

  List<IntermediateJoinColumn> buildInverseJoinColumns(final IntermediateNavigationProperty mappedProperty)
      throws ODataJPAModelException {

    if (this.intermediateMappedProperty == null)
      intermediateMappedProperty = mappedProperty;
    if (inverseJoinColumns == null) {
      inverseJoinColumns = new ArrayList<>(jpaJoinTable.inverseJoinColumns().length);
      for (JoinColumn column : jpaJoinTable.inverseJoinColumns()) {
        if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty())
          if (jpaJoinTable.joinColumns().length > 1)
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
                intermediateMappedProperty.getInternalName());
          else
            inverseJoinColumns.add(new IntermediateJoinColumn(
                ((IntermediateProperty) ((IntermediateEntityType) intermediateMappedProperty.getSourceType()).getKey()
                    .get(0)).getDBFieldName(), column.name()));
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

  List<IntermediateJoinColumn> getInverseJoinColumns() {
    return inverseJoinColumns;
  }
}