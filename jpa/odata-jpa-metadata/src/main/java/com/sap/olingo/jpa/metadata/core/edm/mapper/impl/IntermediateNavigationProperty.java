package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeConverter;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.mapper.annotation.AppliesTo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;

/**
 * A navigation property describes a relation of one entity type to another entity type and allows to navigate to it.
 * IntermediateNavigationProperty represents a navigation within on service, that is source and target are described by
 * the same service document.
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397962"
 * >OData Version 4.0 Part 3 - 7 Navigation Property</a>
 * @author Oliver Grande
 *
 */
final class IntermediateNavigationProperty extends IntermediateModelElement implements
    IntermediateNavigationPropertyAccess, JPAAssociationAttribute {

  private final Attribute<?, ?> jpaAttribute;
  private CsdlNavigationProperty edmNaviProperty;
  private CsdlOnDelete edmOnDelete;
  private final IntermediateStructuredType sourceType;
  private IntermediateStructuredType targetType;
  private JPAAssociationAttribute partner;
  private IntermediateJoinTable joinTable;
  private final IntermediateSchema schema;
  private final List<IntermediateJoinColumn> joinColumns = new ArrayList<>();

  IntermediateNavigationProperty(final JPAEdmNameBuilder nameBuilder, final IntermediateStructuredType parent,
      final Attribute<?, ?> jpaAttribute, final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, IntNameBuilder.buildAssociationName(jpaAttribute));
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    this.sourceType = parent;
    buildNaviProperty();

  }

  @Override
  public void addAnnotations(List<CsdlAnnotation> annotations) {
    edmAnnotations.addAll(annotations);
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter() {
    return null;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() {
    return null;
  }

  public JPAJoinTable getJoinTable() {
    return joinTable;
  }

  @Override
  public JPAAssociationAttribute getPartner() {
    return partner;
  }

  @Override
  public JPAAssociationPath getPath() throws ODataJPAModelException {
    return getStructuredType().getAssociationPath(getExternalName());
  }

  @Override
  public CsdlNavigationProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();
  }

  @Override
  public JPAStructuredType getStructuredType() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return sourceType;
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return targetType;
  }

  @Override
  public Class<?> getType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  public boolean isAssociation() {
    return true;
  }

  @Override
  public boolean isCollection() {
    return jpaAttribute.isCollection();
  }

  @Override
  public boolean isComplex() {
    return false;
  }

  @Override
  public boolean isEnum() {
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
  public void setOnDelete(final CsdlOnDelete onDelete) {
    edmOnDelete = onDelete;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      String mappedBy = null;
      boolean isSourceOne = false;
      edmNaviProperty = new CsdlNavigationProperty();
      edmNaviProperty.setName(getExternalName());
      edmNaviProperty.setType(nameBuilder.buildFQN(targetType.getExternalName()));
      edmNaviProperty.setCollection(jpaAttribute.isCollection());
      // Optional --> ReleationAnnotation
      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
        switch (jpaAttribute.getPersistentAttributeType()) {
        case ONE_TO_MANY:
          final OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
          mappedBy = cardinalityOtM.mappedBy();
          isSourceOne = true;
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtM.cascade()));
          break;
        case ONE_TO_ONE:
          final OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
          edmNaviProperty.setNullable(cardinalityOtO.optional());
          mappedBy = cardinalityOtO.mappedBy();
          isSourceOne = true;
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtO.cascade()));
          break;
        case MANY_TO_ONE:
          final ManyToOne cardinalityMtO = annotatedElement.getAnnotation(ManyToOne.class);
          edmNaviProperty.setNullable(cardinalityMtO.optional());
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityMtO.cascade()));
          break;
        case MANY_TO_MANY:
          final ManyToMany cardinalityMtM = annotatedElement.getAnnotation(ManyToMany.class);
          mappedBy = cardinalityMtM.mappedBy();
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityMtM.cascade()));
          break;
        default:
          break;
        }

//      Determine referential constraint
        buildJoinColumns(mappedBy, isSourceOne, annotatedElement);
        determienReferentialConstraints(annotatedElement);
      }
      // TODO determine ContainsTarget
      determinePartner(mappedBy);
    }

  }

  @Override
  CsdlNavigationProperty getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmNaviProperty;
  }

  PersistentAttributeType getJoinCardinality() throws ODataJPAModelException {
    return jpaAttribute.getPersistentAttributeType();
  }

  List<IntermediateJoinColumn> getJoinColumns() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return joinColumns;
  }

  IntermediateStructuredType getSourceType() {
    return sourceType;
  }

  boolean isMapped() {
    if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      final OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
      return cardinalityOtO.mappedBy() != null && !cardinalityOtO.mappedBy().isEmpty() ? true : false;
    }
    if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_MANY) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      final OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
      return cardinalityOtM.mappedBy() != null && !cardinalityOtM.mappedBy().isEmpty() ? true : false;
    }
    return false;
  }

  private void buildJoinColumns(final String mappedBy, final boolean isSourceOne,
      final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    if (mappedBy != null && !mappedBy.isEmpty()) {
      // Get
      joinTable = ((IntermediateJoinTable) ((IntermediateNavigationProperty) targetType.getAssociation(
          mappedBy)).getJoinTable());
      //
      joinColumns.addAll(joinTable == null ? buildJoinColumnsMapped(mappedBy) : joinTable
          .buildInverseJoinColumns());
      //
      joinTable = joinTable == null ? null : joinTable.asMapped(this);
    } else
      joinColumns.addAll(joinTable == null ? buildJoinColumnsFromAnnotations(isSourceOne, annotatedElement) : joinTable
          .buildJoinColumns());
  }

  private List<IntermediateJoinColumn> buildJoinColumnsFromAnnotations(final boolean isSourceOne,
      final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    int implicitColumns = 0;
    final List<IntermediateJoinColumn> result = new ArrayList<>();
    final JoinColumn[] columns = annotatedElement.getAnnotation(JoinColumns.class) != null ? annotatedElement
        .getAnnotation(JoinColumns.class).value() : null;

    if (columns != null) {
      for (final JoinColumn column : columns) {
        final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(column);
        final String refColumnName = intermediateColumn.getReferencedColumnName();
        final String name = intermediateColumn.getName();
        if (refColumnName == null || refColumnName.isEmpty() || name == null || name.isEmpty()) {
          implicitColumns += 1;
          if (implicitColumns > 1)
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
                getInternalName());
          fillMissingName(isSourceOne, intermediateColumn);
        }
        result.add(intermediateColumn);
      }
    } else {
      final JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
      if (column != null) {
        final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(column);
        fillMissingName(isSourceOne, intermediateColumn);
        joinColumns.add(intermediateColumn);

      }
    }
    return result;
  }

  private List<IntermediateJoinColumn> buildJoinColumnsMapped(final String mappedBy) throws ODataJPAModelException {

    int implicitColumns = 0;
    final List<IntermediateJoinColumn> result = new ArrayList<>();
    result.addAll(targetType.getJoinColumns(mappedBy));
    for (final IntermediateJoinColumn intermediateColumn : result) {
      final String refColumnName = intermediateColumn.getReferencedColumnName();
      if (refColumnName == null || refColumnName.isEmpty()) {
        implicitColumns += 1;
        if (implicitColumns > 1)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
              getInternalName());
        intermediateColumn.setReferencedColumnName(((IntermediateProperty) ((IntermediateEntityType) sourceType)
            .getKey().get(0)).getDBFieldName());
      }
    }
    return result;
  }

  private void buildNaviProperty() throws ODataJPAModelException {
    this.setExternalName(nameBuilder.buildNaviPropertyName(jpaAttribute));
    Class<?> targetClass = null;
    if (jpaAttribute.isCollection()) {
      targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
    } else {
      targetClass = jpaAttribute.getJavaType();
    }
    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
      final javax.persistence.JoinTable jpaJoinTable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
          .getAnnotation(javax.persistence.JoinTable.class);
      joinTable = jpaJoinTable != null ? new IntermediateJoinTable(this, jpaJoinTable, schema) : null;
    }

    targetType = schema.getEntityType(targetClass);
    if (targetType == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NAVI_PROPERTY_NOT_FOUND,
          jpaAttribute.getName(), sourceType.internalName);
    postProcessor.processNavigationProperty(this, jpaAttribute.getDeclaringType().getJavaType()
        .getCanonicalName());
    // Process annotations after post processing, as external name could have been changed
    getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName, AppliesTo.NAVIGATION_PROPERTY);
  }

  private void determienReferentialConstraints(final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    final AssociationOverride overwrite = annotatedElement.getAnnotation(AssociationOverride.class);
    if (overwrite != null || joinTable != null)
      return;

    final List<CsdlReferentialConstraint> constraints = edmNaviProperty.getReferentialConstraints();
    for (final IntermediateJoinColumn intermediateColumn : joinColumns) {

      final CsdlReferentialConstraint constraint = new CsdlReferentialConstraint();
      constraints.add(constraint);
      IntermediateModelElement p = null;
      p = sourceType.getPropertyByDBField(intermediateColumn.getName());
      if (p != null) {
        constraint.setProperty(p.getExternalName());
        p = targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
        if (p != null)
          constraint.setReferencedProperty(p.getExternalName());
        else
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND,
              getInternalName(), intermediateColumn.getReferencedColumnName(), targetType.getExternalName());
      } else {
        p = sourceType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
        if (p != null) {
          constraint.setProperty(p.getExternalName());
          p = targetType.getPropertyByDBField(intermediateColumn.getName());
          if (p != null)
            constraint.setReferencedProperty(p.getExternalName());
          else
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND,
                getInternalName(), intermediateColumn.getName(), targetType.getExternalName());

        } else
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND,
              getInternalName(), intermediateColumn.getReferencedColumnName(), sourceType.getExternalName());
      }
    }
  }

  private void determinePartner(String mappedBy) throws ODataJPAModelException {
    if (sourceType instanceof IntermediateEntityType) {
      // Partner Attribute must not be defined at Complex Types.
      // JPA bi-directional associations are defined at both sides, e.g.
      // at the BusinessPartner and at the Roles. JPA only defines the
      // "mappedBy" at the Parent.
      if (mappedBy != null && !mappedBy.isEmpty()) {
        partner = targetType.getAssociation(mappedBy);
        edmNaviProperty.setPartner(partner.getExternalName());
      } else {
        partner = targetType.getCorrespondingAssiciation(sourceType, getInternalName());
        if (partner != null
            && ((IntermediateNavigationProperty) partner).isMapped()) {
          edmNaviProperty.setPartner(partner.getExternalName());
        }
      }
    }
  }

  private void fillMissingName(final boolean isSourceOne, final IntermediateJoinColumn intermediateColumn)
      throws ODataJPAModelException {

    final String refColumnName = intermediateColumn.getReferencedColumnName();
    final String name = intermediateColumn.getName();

    if (isSourceOne && (refColumnName == null || refColumnName.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) sourceType)
          .getKey().get(0)).getDBFieldName());
    else if (isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (refColumnName == null || refColumnName.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) sourceType)
          .getKey().get(0)).getDBFieldName());
  }

  private CsdlOnDelete setJPAOnDelete(final CascadeType[] cascades) {
    for (final CascadeType cascade : cascades) {
      if (cascade == CascadeType.REMOVE || cascade == CascadeType.ALL) {
        final CsdlOnDelete onDelete = new CsdlOnDelete();
        onDelete.setAction(CsdlOnDeleteAction.Cascade);
        return onDelete;
      }
    }
    return null;
  }
}
