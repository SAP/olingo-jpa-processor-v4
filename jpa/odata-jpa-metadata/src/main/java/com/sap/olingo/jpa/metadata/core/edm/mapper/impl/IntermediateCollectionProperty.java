package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS_COMPEX;
import static javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.JoinColumn;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

/**
 * Represents a collection property. That is a property that may occur more than once.
 * <p>For details about Complex Type metadata see:
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752525"
 * >OData Version 4.0 Part 3 - 9 Complex Type</a>
 * @author Oliver Grande
 *
 */

class IntermediateCollectionProperty extends IntermediateProperty implements JPACollectionAttribute,
    JPAAssociationAttribute {
  private final IntermediateStructuredType sourceType;
  private JPAJoinTable joinTable; // lazy builded
  private JPAAssociationPathImpl associationPath; // lazy builded
  private final JPAPath path;

  /**
   * Copy with in new context
   * @param jpaElement
   * @param intermediateStructuredType
   * @throws ODataJPAModelException
   */
  public IntermediateCollectionProperty(final IntermediateCollectionProperty original,
      final IntermediateStructuredType parent, final IntermediateProperty pathRoot) throws ODataJPAModelException {

    super(original.nameBuilder, original.jpaAttribute, original.schema);
    this.sourceType = parent;

    final List<JPAElement> newPath = new ArrayList<>();
    newPath.add(pathRoot);
    if (original.path != null) {
      newPath.addAll(original.path.getPath());
      this.path = new JPAPathImpl(pathRoot.getExternalName() + JPAPath.PATH_SEPERATOR + original.path.getAlias(), "",
          newPath);
    } else {
      newPath.add(this);
      this.path = new JPAPathImpl(pathRoot.getExternalName() + JPAPath.PATH_SEPERATOR + original.getExternalName(), "",
          newPath);
    }
  }

  IntermediateCollectionProperty(final JPAEdmNameBuilder nameBuilder,
      final PluralAttribute<?, ?, ?> jpaAttribute, final IntermediateSchema schema,
      final IntermediateStructuredType parent) throws ODataJPAModelException {

    super(nameBuilder, jpaAttribute, schema);
    this.sourceType = parent;
    this.path = null;
  }

  @Override
  public JPAAssociationPath asAssociation() throws ODataJPAModelException {
    if (this.associationPath == null)
      this.associationPath = new JPAAssociationPathImpl(this, sourceType, path == null ? sourceType.getPath(
          getExternalName()) : path, ((IntermediateCollectionTable) getJoinTable()).getLeftJoinColumns());
    return associationPath;

  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public boolean isComplex() {
    return getRowType().getPersistenceType() == EMBEDDABLE ? true : false;
  }

  @Override
  public boolean isEtag() {
    return false;
  }

  @Override
  public boolean isKey() {
    return false;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    super.lazyBuildEdmItem();
    if (isComplex()
        && schema.getComplexType(this.edmProperty.getTypeAsFQNObject().getName()) == null)
      // Base type of collection '%1$s' of structured type '%2$s' not found
      throw new ODataJPAModelException(MessageKeys.INVALID_COLLECTION_TYPE, getInternalName(), sourceType
          .getInternalName());
    edmProperty.setCollection(true);
  }

  @Override
  Class<?> determineEntityType() {
    return getRowType().getJavaType();
  }

  @Override
  void determineIsVersion() {
    isVersion = false; // Version is always false
  }

  @Override
  void determineStreamInfo() throws ODataJPAModelException {
    // Stream properties not supported
  }

  @Override
  void determineStructuredType() {
    if (getRowType().getPersistenceType() == PersistenceType.EMBEDDABLE)
      type = schema.getStructuredType((PluralAttribute<?, ?, ?>) jpaAttribute);
    else
      type = null;
  }

  @Override
  FullQualifiedName determineType() throws ODataJPAModelException {
    return determineTypeByPersistanceType(getRowType().getPersistenceType());
  }

  @Override
  String getDeafultValue() throws ODataJPAModelException {
    // No defaults for collection properties
    return null;
  }

  JPAJoinTable getJoinTable() throws ODataJPAModelException {
    if (joinTable == null) {
      final javax.persistence.CollectionTable jpaJoinTable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
          .getAnnotation(javax.persistence.CollectionTable.class);
      joinTable = jpaJoinTable != null ? new IntermediateCollectionTable(jpaJoinTable, schema) : null;
    }
    return joinTable;
  }

  IntermediateStructuredType getSourceType() {
    return sourceType;
  }

  @Override
  boolean isStream() {
    // OData Version 4.0. Part 3: Common Schema Definition Language (CSDL) Plus Errata 03:
    // Edm.Stream, or a type definition whose underlying type is Edm.Stream, cannot be used in collections or for
    // non-binding parameters to functions or actions.
    return false;
  }

  private Type<?> getRowType() {
    return ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType();
  }

  private class IntermediateCollectionTable implements JPAJoinTable {
    private final CollectionTable jpaJoinTable;
    private List<IntermediateJoinColumn> joinColumns;
    private final JPAEntityType jpaEntityType;

    public IntermediateCollectionTable(final CollectionTable jpaJoinTable, final IntermediateSchema schema)
        throws ODataJPAModelException {
      super();
      this.jpaJoinTable = jpaJoinTable;
      this.jpaEntityType = schema.getEntityType(jpaJoinTable.catalog(), jpaJoinTable.schema(), jpaJoinTable.name());
      this.joinColumns = buildJoinColumns(sourceType);
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
    public JPAEntityType getEntityType() {
      return jpaEntityType;
    }

    @Override
    public String getInverseAlias(String dbFieldName) {
      return null;
    }

    @Override
    public List<JPAOnConditionItem> getInversJoinColumns() throws ODataJPAModelException {
      final List<JPAOnConditionItem> result = new ArrayList<>();

      for (IntermediateJoinColumn column : joinColumns) {
        result.add(new JPAOnConditionItemImpl(
            ((IntermediateEntityType) jpaEntityType).getPathByDBField(column.getReferencedColumnName()),
            sourceType.getPathByDBField(column.getName())));
      }
      return result;
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
    public String getTableName() {
      return jpaJoinTable.name();
    }

    List<IntermediateJoinColumn> getLeftJoinColumns() throws ODataJPAModelException {
      return buildJoinColumns(sourceType);
    }

    private List<IntermediateJoinColumn> buildJoinColumns(final IntermediateStructuredType contextType)
        throws ODataJPAModelException {

      final List<IntermediateJoinColumn> result = new ArrayList<>();
      for (JoinColumn column : jpaJoinTable.joinColumns()) {
        if (column.referencedColumnName() == null || column.referencedColumnName().isEmpty())
          if (jpaJoinTable.joinColumns().length > 1)
            throw new ODataJPAModelException(NOT_SUPPORTED_NO_IMPLICIT_COLUMNS, getInternalName());
          else if (!(contextType instanceof IntermediateEntityType))
            throw new ODataJPAModelException(NOT_SUPPORTED_NO_IMPLICIT_COLUMNS_COMPEX, contextType.getInternalName());
          else {
            result.add(new IntermediateJoinColumn(
                ((IntermediateProperty) ((IntermediateEntityType) contextType).getKey().get(0))
                    .getDBFieldName(), column.name()));
          }
        else
          result.add(new IntermediateJoinColumn(column.referencedColumnName(), column.name()));
      }
      return result;
    }
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    return joinTable.getEntityType();
  }

  @Override
  public JPAAssociationAttribute getPartner() {
    return null;
  }

  @Override
  public JPAAssociationPath getPath() throws ODataJPAModelException {
    return asAssociation();
  }
}
