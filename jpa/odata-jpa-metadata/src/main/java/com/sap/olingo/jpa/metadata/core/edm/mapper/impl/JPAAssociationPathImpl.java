package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
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
  private final IntermediateStructuredType<?> sourceType;
  private final IntermediateStructuredType<?> targetType;
  private final List<? extends JPAJoinColumn> joinColumns;
  private final PersistentAttributeType cardinality;
  private final boolean isCollection;
  private final JPAAssociationAttribute partner;
  private final JPAJoinTable joinTable;

  JPAAssociationPathImpl(final IntermediateNavigationProperty<?> association,
      final IntermediateStructuredType<?> source) throws ODataJPAModelException {

    final List<JPAElement> pathElementsBuffer = new ArrayList<>();
    pathElementsBuffer.add(association);

    alias = association.getExternalName();
    this.sourceType = source;
    this.targetType = (IntermediateStructuredType<?>) association.getTargetEntity();
    this.joinColumns = association.getJoinColumns();
    this.pathElements = Collections.unmodifiableList(pathElementsBuffer);
    this.cardinality = association.getJoinCardinality();
    this.isCollection = association.isCollection();
    this.partner = association.getPartner();
    this.joinTable = association.getJoinTable();
  }

  JPAAssociationPathImpl(final JPAAssociationPath associationPath, final IntermediateStructuredType<?> source,
      final List<? extends JPAJoinColumn> joinColumns, final JPAAttribute attribute) throws ODataJPAModelException {

    final List<JPAElement> pathElementsBuffer = new ArrayList<>();
    pathElementsBuffer.add(attribute);
    pathElementsBuffer.addAll(associationPath.getPath());

    alias = buildNavigationPropertyBindingName(associationPath, attribute);
    this.sourceType = source;
    this.targetType = (IntermediateStructuredType<?>) associationPath.getTargetType();
    if (joinColumns.isEmpty())
      this.joinColumns = ((JPAAssociationPathImpl) associationPath).getJoinColumns();
    else
      this.joinColumns = joinColumns;
    this.pathElements = Collections.unmodifiableList(pathElementsBuffer);
    this.cardinality = ((JPAAssociationPathImpl) associationPath).getCardinality();
    this.isCollection = associationPath.isCollection();
    this.partner = associationPath.getPartner();
    this.joinTable = associationPath.hasJoinTable()
        ? ((IntermediateJoinTable) associationPath.getJoinTable()).withSource(source)
        : null;

  }

  /**
   * Collection Properties
   * @param collectionProperty
   * @param source
   * @param path
   * @param joinColumns
   * @throws ODataJPAModelException
   */
  public JPAAssociationPathImpl(final IntermediateCollectionProperty<?> collectionProperty,
      final IntermediateStructuredType<?> source, final JPAPath path, final List<? extends JPAJoinColumn> joinColumns)
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

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    if (joinTable instanceof final IntermediateJoinTable intermediateJoinTable)
      for (final IntermediateJoinColumn column : intermediateJoinTable.buildInverseJoinColumns()) {
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
    for (final JPAJoinColumn column : this.joinColumns) {
      result.add(new JPAOnConditionItemImpl(
          sourceType.getPathByDBField(column.getName()),
          targetType.getPathByDBField(column.getReferencedColumnName())));
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
    for (final JPAJoinColumn column : this.joinColumns) {
      final JPAPath columnPath = sourceType.getPathByDBField(column.getName());
      if (columnPath != null)
        result.add(columnPath);
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
  public String getPathAsString() {
    return getAlias();
  }

  @Override
  public List<JPAPath> getRightColumnsList() throws ODataJPAModelException {
    final List<JPAPath> result = new ArrayList<>();
    for (final JPAJoinColumn column : this.joinColumns) {
      final JPAPath columnPath = targetType.getPathByDBField(column.getReferencedColumnName());
      if (columnPath != null)
        result.add(columnPath);
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

  private List<? extends JPAJoinColumn> getJoinColumns() {
    return joinColumns;
  }

  /**
   * A navigation property binding MUST name a navigation property of the
   * entity set’s, singleton's, or containment navigation property's entity
   * type or one of its subtypes in the Path attribute. If the navigation
   * property is defined on a subtype, the path attribute MUST contain the
   * QualifiedName of the subtype, followed by a forward slash, followed by
   * the navigation property name. If the navigation property is defined on
   * a complex type used in the definition of the entity set’s entity type,
   * the path attribute MUST contain a forward-slash separated list of complex
   * property names and qualified type names that describe the path leading
   * to the navigation property. See <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398035">
   * Navigation Property Binding</a>.
   * @param associationPath
   * @param parent
   * @return non empty unique name of a Navigation Property Binding
   */
  // TODO respect subtype name
  @Nonnull
  private String buildNavigationPropertyBindingName(final JPAAssociationPath associationPath,
      final JPAAttribute parent) {
    final StringBuilder name = new StringBuilder();

    name.append(parent.getExternalName());
    for (final JPAElement pathElement : associationPath.getPath()) {
      name.append(JPAPath.PATH_SEPARATOR);
      name.append(pathElement.getExternalName());

    }
    return name.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath#hasJoinTable()
   */
  @Override
  public boolean hasJoinTable() {
    return joinTable != null;
  }
}
