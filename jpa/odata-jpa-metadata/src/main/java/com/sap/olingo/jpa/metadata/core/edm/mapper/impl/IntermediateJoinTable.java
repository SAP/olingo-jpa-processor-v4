package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NO_JOIN_TABLE_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateJoinTable implements JPAJoinTable {
  private static final Log LOGGER = LogFactory.getLog(IntermediateJoinTable.class);

  private final IntermediateNavigationProperty<?> intermediateProperty;
  private final JoinTable jpaJoinTable;
  private final IntermediateStructuredType<?> sourceType;
  private List<IntermediateJoinColumn> joinColumns = null;
  private List<IntermediateJoinColumn> inverseJoinColumns = null;
  private final Optional<JPAEntityType> jpaEntityType;

  IntermediateJoinTable(final IntermediateNavigationProperty<?> intermediateProperty, final JoinTable jpaJoinTable,
      final IntermediateSchema schema) throws ODataJPAModelException {
    super();
    this.intermediateProperty = intermediateProperty;
    this.jpaJoinTable = jpaJoinTable;
    this.sourceType = intermediateProperty.getSourceType();
    this.jpaEntityType = Optional.ofNullable(schema.getEntityType(jpaJoinTable.catalog(), jpaJoinTable.schema(),
        jpaJoinTable.name()));
    LOGGER.trace("Determined entity type of join table: "
        + jpaEntityType.map(JPAEntityType::getInternalName).orElse("null"));
  }

  private IntermediateJoinTable(final IntermediateJoinTable intermediateJoinTable,
      final IntermediateNavigationProperty<?> mappedBy) throws ODataJPAModelException {

    this.jpaJoinTable = intermediateJoinTable.jpaJoinTable;
    this.sourceType = intermediateJoinTable.getTargetType();
    this.jpaEntityType = intermediateJoinTable.jpaEntityType;
    this.intermediateProperty = mappedBy;
    this.joinColumns = intermediateJoinTable.buildInverseJoinColumns();
    this.inverseJoinColumns = intermediateJoinTable.buildJoinColumns();
  }

  private IntermediateJoinTable(final IntermediateJoinTable intermediateJoinTable,
      final IntermediateStructuredType<?> sourceType) throws ODataJPAModelException {
    this.jpaJoinTable = intermediateJoinTable.jpaJoinTable;
    this.sourceType = sourceType;
    this.jpaEntityType = intermediateJoinTable.jpaEntityType;
    this.intermediateProperty = intermediateJoinTable.intermediateProperty;
    this.inverseJoinColumns = intermediateJoinTable.buildInverseJoinColumns();
    buildJoinColumns();
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType.orElse(null);
  }

  @Override
  public List<JPAOnConditionItem> getInverseJoinColumns() throws ODataJPAModelException {

    buildInverseJoinColumns();
    final IntermediateStructuredType<?> targetType = (IntermediateStructuredType<?>) intermediateProperty
        .getTargetEntity();
    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (final IntermediateJoinColumn column : inverseJoinColumns) {
      result.add(new JPAOnConditionItemImpl(
          ((IntermediateEntityType<?>) jpaEntityType
              .orElseThrow(() -> new ODataJPAModelException(NO_JOIN_TABLE_TYPE)))
                  .getPathByDBField(column.getReferencedColumnName()),
          targetType.getPathByDBField(column.getName())));
    }
    return result;
  }

  @Override
  public List<JPAOnConditionItem> getJoinColumns() throws ODataJPAModelException {

    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (final IntermediateJoinColumn column : buildJoinColumns()) {
      result.add(new JPAOnConditionItemImpl(
          sourceType.getPathByDBField(column.getName()),
          ((IntermediateEntityType<?>) jpaEntityType
              .orElseThrow(() -> new ODataJPAModelException(NO_JOIN_TABLE_TYPE)))
                  .getPathByDBField(column.getReferencedColumnName())));
    }
    return result;
  }

  @Override
  public List<JPAPath> getRightColumnsList() throws ODataJPAModelException {
    return getInverseJoinColumns().stream()
        .map(JPAOnConditionItem::getRightPath)
        .toList();
  }

  @Override
  public List<JPAPath> getLeftColumnsList() throws ODataJPAModelException {

    return getJoinColumns().stream()
        .map(JPAOnConditionItem::getLeftPath)
        .toList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends JPAJoinColumn> List<T> getRawInverseJoinInformation() throws ODataJPAModelException {
    buildInverseJoinColumns();
    // For criteria builder the columns get switched
    final List<T> invertedColumns = new ArrayList<>(inverseJoinColumns.size());
    for (final IntermediateJoinColumn column : inverseJoinColumns) {
      invertedColumns.add((T) new IntermediateJoinColumn(column.getReferencedColumnName(), column.getName()));
    }
    return invertedColumns;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends JPAJoinColumn> List<T> getRawJoinInformation() {
    return (List<T>) joinColumns;
  }

  /**
   * Returns the name of database table
   * @return
   */
  @Override
  public String getTableName() {
    return buildFQTableName(jpaJoinTable.schema(), jpaJoinTable.name());
  }

  protected final String buildFQTableName(final String schema, final String table) {
    final StringBuilder fqt = new StringBuilder();
    if (schema != null && !schema.isEmpty()) {
      fqt.append(schema);
      fqt.append(".");
    }
    fqt.append(table);
    return fqt.toString();
  }

  IntermediateJoinTable asMapped(final IntermediateNavigationProperty<?> mappedBy) throws ODataJPAModelException {
    return new IntermediateJoinTable(this, mappedBy);
  }

  List<IntermediateJoinColumn> buildInverseJoinColumns() throws ODataJPAModelException {

    if (inverseJoinColumns == null) {
      inverseJoinColumns = new ArrayList<>(jpaJoinTable.inverseJoinColumns().length);
      for (final JoinColumn column : jpaJoinTable.inverseJoinColumns()) {
        buildInverseJoinColumn(column);
      }
    }
    return inverseJoinColumns;
  }

  List<IntermediateJoinColumn> buildJoinColumns() throws ODataJPAModelException {
    if (joinColumns == null) {
      joinColumns = new ArrayList<>(jpaJoinTable.inverseJoinColumns().length);

      for (final JoinColumn column : jpaJoinTable.joinColumns()) {
        buildJoinColumn(column);
      }
    }
    return joinColumns;
  }

  IntermediateJoinTable withSource(final IntermediateStructuredType<?> sourceType) throws ODataJPAModelException {
    return new IntermediateJoinTable(this, sourceType);
  }

  private void buildInverseJoinColumn(final JoinColumn column) throws ODataJPAModelException {
    if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty()) {
      if (jpaJoinTable.joinColumns().length > 1)
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
            intermediateProperty.getInternalName());
      else
        inverseJoinColumns.add(new IntermediateJoinColumn(
            ((IntermediateProperty) ((IntermediateEntityType<?>) getTargetType()).getKey().get(0)).getDBFieldName(),
            column.name()));
    } else {
      inverseJoinColumns.add(new IntermediateJoinColumn(column.referencedColumnName(), column.name()));
    }
  }

  private void buildJoinColumn(final JoinColumn column) throws ODataJPAModelException {
    if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty()) {
      if (jpaJoinTable.joinColumns().length > 1)
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
            intermediateProperty.getInternalName());
      else if (!(intermediateProperty.getSourceType() instanceof IntermediateEntityType))
        throw new ODataJPAModelException(
            ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS_COMPLEX,
            intermediateProperty.getInternalName());
      else {
        joinColumns.add(new IntermediateJoinColumn(
            ((IntermediateProperty) ((IntermediateEntityType<?>) intermediateProperty.getSourceType()).getKey().get(
                0)).getDBFieldName(), column.name()));
      }
    } else {
      joinColumns.add(new IntermediateJoinColumn(column.referencedColumnName(), column.name()));
    }
  }

  private IntermediateStructuredType<?> getTargetType() throws ODataJPAModelException {
    return (IntermediateStructuredType<?>) intermediateProperty.getTargetEntity();
  }
}