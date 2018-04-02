package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

final class JPAAssociationPathImpl implements JPAAssociationPath {
  private final String alias;
  private final List<JPAElement> pathElements;
  private final IntermediateStructuredType sourceType;
  private final IntermediateStructuredType targetType;
  private final List<IntermediateJoinColumn> joinColumns;
  private final PersistentAttributeType cardinality;
  private final boolean isCollection;
  private final JPAAssociationAttribute partner;
  private final JPAJoinTable joinTable;

  JPAAssociationPathImpl(final IntermediateNavigationProperty association,
      final IntermediateStructuredType source) throws ODataJPAModelException {

    final List<JPAElement> pathElementsBuffer = new ArrayList<>();
    pathElementsBuffer.add(association);

    alias = association.getExternalName();
    this.sourceType = source;
    this.targetType = (IntermediateStructuredType) association.getTargetEntity();
    this.joinColumns = association.getJoinColumns();
    this.pathElements = Collections.unmodifiableList(pathElementsBuffer);
    this.cardinality = association.getJoinCardinality();
    this.isCollection = association.isCollection();
    this.partner = association.getPartner();
    this.joinTable = association.getJoinTable();
  }

  JPAAssociationPathImpl(final JPAEdmNameBuilder namebuilder, final JPAAssociationPath associationPath,
      final IntermediateStructuredType source, final List<IntermediateJoinColumn> joinColumns,
      final JPAAttribute attribute) {

    final List<JPAElement> pathElementsBuffer = new ArrayList<>();
    pathElementsBuffer.add(attribute);
    pathElementsBuffer.addAll(associationPath.getPath());

    alias = namebuilder.buildNaviPropertyBindingName(associationPath, attribute);
    this.sourceType = source;
    this.targetType = (IntermediateStructuredType) associationPath.getTargetType();
    if (joinColumns.isEmpty())
      this.joinColumns = ((JPAAssociationPathImpl) associationPath).getJoinColumns();
    else
      this.joinColumns = joinColumns;
    this.pathElements = Collections.unmodifiableList(pathElementsBuffer);
    this.cardinality = ((JPAAssociationPathImpl) associationPath).getCardinality();
    this.isCollection = associationPath.isCollection();
    this.partner = associationPath.getPartner();
    this.joinTable = associationPath.getJoinTable();
  }

  /**
   * Collection Properties
   * @param collectionProperty
   * @param source
   * @param path
   * @param joinColumns
   * @throws ODataJPAModelException
   */
  public JPAAssociationPathImpl(final IntermediateCollectionProperty collectionProperty,
      final IntermediateStructuredType source, final JPAPath path, final List<IntermediateJoinColumn> joinColumns)
      throws ODataJPAModelException {

    alias = path.getAlias();
    this.sourceType = source;
    this.targetType = null;
    this.joinColumns = joinColumns;
    this.pathElements = path.getPath();
    this.cardinality = PersistentAttributeType.ONE_TO_MANY;
    this.isCollection = true;
    this.partner = null;
    this.joinTable = collectionProperty.getJoinTable();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getAlias()
   */
  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    if (joinTable instanceof IntermediateJoinTable)
      for (final IntermediateJoinColumn column : ((IntermediateJoinTable) joinTable).buildInverseJoinColumns()) {
      result.add(targetType.getPathByDBField(column.getName()));
      }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getJoinColumnsList()
   */
  @Override
  public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
    final List<JPAOnConditionItem> result = new ArrayList<>();
    for (final IntermediateJoinColumn column : this.joinColumns) {
      // ManyToOne
      if (cardinality == PersistentAttributeType.MANY_TO_ONE
          || cardinality == PersistentAttributeType.MANY_TO_MANY)
        result.add(new JPAOnConditionItemImpl(
            sourceType.getPathByDBField(column.getName()),
            targetType.getPathByDBField(column.getReferencedColumnName())));
      else
        result.add(new JPAOnConditionItemImpl(
            sourceType.getPathByDBField(column.getReferencedColumnName()),
            targetType.getPathByDBField(column.getName())));
    }
    return result;
  }

  @Override
  public JPAJoinTable getJoinTable() {
    return joinTable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getLeaf()
   */
  @Override
  public JPAAssociationAttribute getLeaf() {
    return (JPAAssociationAttribute) pathElements.get(pathElements.size() - 1);
  }

  @Override
  public List<JPAPath> getLeftColumnsList() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    for (final IntermediateJoinColumn column : this.joinColumns) {
      // ManyToOne
      if (joinTable != null
          || (cardinality == PersistentAttributeType.MANY_TO_ONE))
        result.add(sourceType.getPathByDBField(column.getName()));
      else
        result.add(sourceType.getPathByDBField(column.getReferencedColumnName()));
    }
    return result;
  }

  @Override
  public JPAAssociationAttribute getPartner() {
    return partner;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getPath()
   */
  @Override
  public List<JPAElement> getPath() {
    return pathElements;
  }

  @Override
  public List<JPAPath> getRightColumnsList() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    for (final IntermediateJoinColumn column : this.joinColumns) {
      // ManyToOne
      if (cardinality == PersistentAttributeType.MANY_TO_ONE)
        result.add(targetType.getPathByDBField(column.getReferencedColumnName()));
      else
        result.add(targetType.getPathByDBField(column.getName()));
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getSourceType()
   */
  @Override
  public JPAStructuredType getSourceType() {
    return sourceType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#getTargetType()
   */
  @Override
  public JPAStructuredType getTargetType() {
    return targetType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#isCollection()
   */
  @Override
  public boolean isCollection() {
    return isCollection;
  }

  @Override
  public String toString() {
    return "JPAAssociationPathImpl [alias=" + alias + ", pathElements=" + pathElements + ", sourceType=" + sourceType
        + ", targetType=" + targetType + ", joinColumns=" + joinColumns + ", cardinality=" + cardinality
        + ", joinTable=" + joinTable + "]";
  }

  private PersistentAttributeType getCardinality() {
    return cardinality;
  }

  private List<IntermediateJoinColumn> getJoinColumns() {
    return joinColumns;
  }

}
