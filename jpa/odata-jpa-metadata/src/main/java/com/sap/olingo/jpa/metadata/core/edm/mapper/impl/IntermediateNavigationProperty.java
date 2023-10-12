package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.MISSING_ONE_TO_ONE_ANNOTATION;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelIgnoreException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.PluralAttribute;

/**
 * A navigation property describes a relation of one entity type to another entity type and allows to navigate to it.
 * IntermediateNavigationProperty represents a navigation within on service, that is source and target are described by
 * the same service document.
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397962"
 * >OData Version 4.0 Part 3 - 7 Navigation Property</a>
 * @author Oliver Grande
 * @param <S> Type of the parent the navigation belongs to, also called source type
 */
final class IntermediateNavigationProperty<S> extends IntermediateModelElement implements
    IntermediateNavigationPropertyAccess, JPAAssociationAttribute {

  private static final Log LOGGER = LogFactory.getLog(IntermediateNavigationProperty.class);

  private final Attribute<?, ?> jpaAttribute;
  private CsdlNavigationProperty edmNaviProperty;
  private CsdlOnDelete edmOnDelete;
  private final IntermediateStructuredType<S> sourceType;
  private IntermediateStructuredType<?> targetType;
  private JPAAssociationAttribute partner;
  private IntermediateJoinTable joinTable;
  private final IntermediateSchema schema;
  private final List<IntermediateJoinColumn> joinColumns = new ArrayList<>();
  private final List<String> requiredDbColumns = new ArrayList<>();
  private PersistentAttributeType cardinality;
  private Optional<String> mappedBy;

  IntermediateNavigationProperty(final JPAEdmNameBuilder nameBuilder, final IntermediateStructuredType<S> parent,
      final Attribute<?, ?> jpaAttribute, final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, InternalNameBuilder.buildAssociationName(jpaAttribute), schema.getAnnotationInformation());
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    this.sourceType = parent;
    buildNaviProperty();
  }

  @Override
  public void addAnnotations(final List<CsdlAnnotation> annotations) {
    edmAnnotations.addAll(annotations);
  }

  @Override
  public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor() {
    return null;
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter() {
    return null;
  }

  @Override
  public Class<?> getDbType() {
    return null;
  }

  @Override
  public Class<?> getJavaType() {
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
  public Set<String> getProtectionClaimNames() {
    return new HashSet<>(0);
  }

  @Override
  public List<String> getProtectionPath(final String claimName) throws ODataJPAModelException {
    return new ArrayList<>(0);
  }

  @Override
  public <X, Y extends Object> AttributeConverter<X, Y> getRawConverter() {
    return null;
  }

  @Override
  public List<String> getRequiredProperties() {
    return Collections.emptyList();
  }

  @Override
  public JPAStructuredType getStructuredType() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      lazyBuildEdmItem();
    }
    return sourceType;
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      lazyBuildEdmItem();
    }
    return targetType;
  }

  @Override
  public Class<?> getType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  public boolean hasProtection() {
    return false;
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
  public boolean isTransient() {
    return false;
  }

  @Override
  public void setOnDelete(final CsdlOnDelete onDelete) {
    edmOnDelete = onDelete;
  }

  @Override
  public String toString() {
    return getExternalName() + ": [sourceType=" + sourceType + ", targetType=" + targetType
        + ", partner=" + partnerToString() + ", joinTable=" + joinTable + "]";
  }

  private String partnerToString() {
    return partner == null ? "null" : partner.getExternalName();
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      edmNaviProperty = new CsdlNavigationProperty();
      edmNaviProperty.setName(getExternalName());
      edmNaviProperty.setType(buildFQN(targetType.getExternalName()));
      edmNaviProperty.setCollection(jpaAttribute.isCollection());
      edmNaviProperty.setAnnotations(edmAnnotations);
      // Optional --> RelationAnnotation
      determineMappedBy();
      buildJoinConditionInfo();
      // TODO determine ContainsTarget
      determinePartner();
    }
  }

  @Override
  CsdlNavigationProperty getEdmItem() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      lazyBuildEdmItem();
    }
    return edmNaviProperty;
  }

  @Override
  public CsdlAnnotation getAnnotation(final String alias, final String term) throws ODataJPAModelException {
    return filterAnnotation(alias, term);
  }

  PersistentAttributeType getJoinCardinality() {
    return jpaAttribute.getPersistentAttributeType();
  }

  @SuppressWarnings("unchecked")
  <T extends JPAJoinColumn> List<T> getJoinColumns() throws ODataJPAModelException {
    if (joinColumns.isEmpty()) {
      lazyBuildEdmItem();
      buildJoinConditionInfo();
    }
    return (List<T>) joinColumns;
  }

  List<String> getRequiredDbColumns() throws ODataJPAModelException {
    if (edmNaviProperty == null)
      lazyBuildEdmItem();
    if (requiredDbColumns.isEmpty()) {
      for (final IntermediateJoinColumn joinColumn : joinColumns) {
        requiredDbColumns.add(joinColumn.getName());
      }
    }
    return requiredDbColumns;
  }

  IntermediateStructuredType<S> getSourceType() {
    return sourceType;
  }

  boolean isMapped() {
    return mappedBy.isPresent();
  }

  /**
   * In case the column name is not given at an association a default name is generated according to the JPA standard.
   * The default name will be &lt;association name&gt;_&lt;target key name&gt;, all upper case.
   * @param annotatedElement
   * @param isSourceOne
   * @return
   * @throws ODataJPAModelException
   */
  private String buildDefaultName(final boolean isSourceOne) throws ODataJPAModelException {

    final StringBuilder columnName = new StringBuilder(jpaAttribute.getName());
    columnName.append('_');
    if (isSourceOne)
      columnName.append(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else
      columnName.append(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
    return columnName.toString().replace("\"", "").toUpperCase(Locale.ENGLISH);
  }

  private IntermediateJoinColumn buildImplicitJoinColumnPair(final boolean isSourceOne) throws ODataJPAModelException {
    final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(buildDefaultName(isSourceOne),
        fillMissingName());
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(getExternalName() + ": Add join condition with default name = " + intermediateColumn.toString());
    return intermediateColumn;
  }

  private void buildJoinColumnPairList(final boolean isSourceOne, int implicitColumns,
      final List<IntermediateJoinColumn> result, final JoinColumn[] columns) throws ODataJPAModelException {
    for (final JoinColumn column : columns) {
      final String refColumnName = column.referencedColumnName();
      final String name = column.name();
      result.add(buildOneJoinColumnPair(isSourceOne, column));
      if (refColumnName == null || refColumnName.isEmpty() || name == null || name.isEmpty()) {
        implicitColumns += 1;
        if (implicitColumns > 1)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
              getInternalName());
      }
    }
  }

  private void buildJoinColumns(final boolean isSourceOne, final AnnotatedElement annotatedElement)
      throws ODataJPAModelException {

    joinColumns.clear();
    final List<IntermediateJoinColumn> columns;
    if (mappedBy.isPresent()) {
      joinTable = ((IntermediateJoinTable) ((IntermediateNavigationProperty<?>) targetType.getAssociation(
          mappedBy.get())).getJoinTable());
      if (joinTable == null)
        columns = buildJoinColumnsMapped(mappedBy.get());
      else
        columns = joinTable.buildInverseJoinColumns();
      joinTable = joinTable == null ? null : joinTable.asMapped(this);
    } else {
      if (joinTable == null)
        columns = buildJoinColumnsFromAnnotations(isSourceOne, annotatedElement);
      else
        columns = joinTable.buildJoinColumns();
    }
    joinColumns.clear();
    joinColumns.addAll(columns);
  }

  private List<IntermediateJoinColumn> buildJoinColumnsFromAnnotations(final boolean isSourceOne,
      final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    final int implicitColumns = 0;
    final List<IntermediateJoinColumn> result = new ArrayList<>();
    final JoinColumn[] columns = annotatedElement.getAnnotation(JoinColumns.class) != null ? annotatedElement
        .getAnnotation(JoinColumns.class).value() : null;

    if (columns != null) {
      buildJoinColumnPairList(isSourceOne, implicitColumns, result, columns);
    } else {
      final JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
      if (column != null) {
        result.add(buildOneJoinColumnPair(isSourceOne, column));
      } else {
        // No explicit Join Columns given: Build implicit one.
        result.add(buildImplicitJoinColumnPair(isSourceOne));
      }
    }
    return result;
  }

  private List<IntermediateJoinColumn> buildJoinColumnsMapped(final String mappedBy) throws ODataJPAModelException {

    int implicitColumns = 0;
    final List<IntermediateJoinColumn> result = invertJoinColumns(targetType.getJoinColumns(mappedBy));
    for (final IntermediateJoinColumn intermediateColumn : result) {
      final String columnName = intermediateColumn.getName();
      if (columnName == null || columnName.isEmpty()) {
        implicitColumns += 1;
        if (implicitColumns > 1)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
              getInternalName());
        intermediateColumn.setName(((IntermediateProperty) ((IntermediateEntityType<?>) sourceType)
            .getKey().get(0)).getDBFieldName());
      }
    }
    return result;
  }

  private void buildJoinConditionInfo() throws ODataJPAModelException {

    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement
        && joinColumns.isEmpty()) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
//    Determine referential constraint
      final boolean isOwner = !mappedBy.isPresent();
      buildJoinColumns(isOwner, annotatedElement);
      determineReferentialConstraints(annotatedElement);
    }
  }

  private void buildNaviProperty() throws ODataJPAModelException {

    this.setExternalName(nameBuilder.buildNaviPropertyName(jpaAttribute));

    evaluateAnnotation();

    targetType = schema.getEntityType(determineTargetClass());
    if (targetType == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NAVI_PROPERTY_NOT_FOUND,
          jpaAttribute.getName(), sourceType.internalName);

    postProcessor.processNavigationProperty(this, jpaAttribute.getDeclaringType().getJavaType()
        .getCanonicalName());
    // Process annotations after post processing, as external name could have been changed
    getAnnotations(edmAnnotations, this.jpaAttribute.getJavaMember(), internalName);

    checkConsistency();
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(toString());
  }

  private void evaluateAnnotation() throws ODataJPAModelException {

    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
      final jakarta.persistence.JoinTable jpaJoinTable = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
          .getAnnotation(jakarta.persistence.JoinTable.class);
      joinTable = jpaJoinTable != null ? new IntermediateJoinTable(this, jpaJoinTable, schema) : null;

      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      cardinality = jpaAttribute.getPersistentAttributeType();
      if (cardinality != null) {
        switch (cardinality) {
          case ONE_TO_MANY:
            // Association '%1$s' of '%2$s' requires a cardinality annotation.
            final OneToMany oneTOMany = Optional.ofNullable(annotatedElement.getAnnotation(OneToMany.class))
                .orElseThrow(() -> new ODataJPAModelException(MISSING_ONE_TO_ONE_ANNOTATION, internalName, sourceType
                    .getInternalName()));
            mappedBy = Optional.ofNullable(returnNullIfEmpty(oneTOMany.mappedBy()));
            break;
          case ONE_TO_ONE:
            // Association '%1$s' of '%2$s' requires a cardinality annotation.
            final OneToOne annotation = Optional.ofNullable(annotatedElement.getAnnotation(OneToOne.class))
                .orElseThrow(() -> new ODataJPAModelException(MISSING_ONE_TO_ONE_ANNOTATION, internalName, sourceType
                    .getInternalName()));
            mappedBy = Optional.ofNullable(returnNullIfEmpty(annotation.mappedBy()));
            break;
          case MANY_TO_MANY:
            mappedBy = Optional.ofNullable(returnNullIfEmpty(
                annotatedElement.getAnnotation(ManyToMany.class).mappedBy()));
            break;
          default:
            mappedBy = Optional.empty();
        }
      } else {
        mappedBy = Optional.empty();
      }
    }
  }

  private Class<?> determineTargetClass() {
    Class<?> targetClass = null;
    if (jpaAttribute.isCollection()) {
      targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
    } else {
      targetClass = jpaAttribute.getJavaType();
    }
    return targetClass;
  }

  private IntermediateJoinColumn buildOneJoinColumnPair(final boolean isSourceOne, final JoinColumn column)
      throws ODataJPAModelException {
    IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(column);
    if (cardinality == PersistentAttributeType.ONE_TO_MANY)
      intermediateColumn = new IntermediateJoinColumn(intermediateColumn.getReferencedColumnName(),
          intermediateColumn.getName());
    fillMissingName(isSourceOne, intermediateColumn);
    if (LOGGER.isTraceEnabled())
      LOGGER.trace(getExternalName() + ": Add join condition = " + intermediateColumn.toString());
    return intermediateColumn;
  }

  private void checkConsistency() throws ODataJPAModelException {
    final EdmProtectedBy jpaProtectedBy = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmProtectedBy.class);
    if (jpaProtectedBy != null) {
      // Navigation Properties do not support EdmProtectedBy
      throw new ODataJPAModelException(MessageKeys.NOT_SUPPORTED_PROTECTED_NAVIGATION, this.sourceType.getTypeClass()
          .getCanonicalName(), this.internalName);
    }
    final EdmVisibleFor jpaFieldGroups = ((AnnotatedElement) this.jpaAttribute.getJavaMember())
        .getAnnotation(EdmVisibleFor.class);
    if (jpaFieldGroups != null) {
      throw new ODataJPAModelException(MessageKeys.NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP,
          this.sourceType.getTypeClass().getCanonicalName(), this.internalName);
    }
  }

  @CheckForNull
  private void determineMappedBy() {

    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      cardinality = jpaAttribute.getPersistentAttributeType();
      switch (cardinality) {
        case ONE_TO_MANY:
          final OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtM.cascade()));
          break;
        case ONE_TO_ONE:
          final OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
          edmNaviProperty.setNullable(cardinalityOtO.optional());
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtO.cascade()));
          break;
        case MANY_TO_ONE:
          final ManyToOne cardinalityMtO = annotatedElement.getAnnotation(ManyToOne.class);
          edmNaviProperty.setNullable(cardinalityMtO.optional());
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityMtO.cascade()));
          break;
        case MANY_TO_MANY:
          final ManyToMany cardinalityMtM = annotatedElement.getAnnotation(ManyToMany.class);
          edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityMtM.cascade()));
          break;
        default:
          break;
      }
    }
  }

  private void determinePartner() throws ODataJPAModelException {
    if (sourceType instanceof IntermediateEntityType) {
      // Partner Attribute must not be defined at Complex Types.
      // JPA bi-directional associations are defined at both sides, e.g.
      // at the BusinessPartner and at the Roles. JPA only defines the
      // "mappedBy" at the Parent.
      if (mappedBy.isPresent()) {
        partner = targetType.getAssociation(mappedBy.get());
        edmNaviProperty.setPartner(partner.getExternalName());
      } else {
        partner = targetType.getCorrespondingAssociation(sourceType, getInternalName());
        if (partner != null
            && ((IntermediateNavigationProperty<?>) partner).isMapped()) {
          edmNaviProperty.setPartner(partner.getExternalName());
        }
      }
      if (LOGGER.isTraceEnabled())
        LOGGER.trace(getExternalName() + ": Found partner = " + partnerToString());
    }
  }

  private Optional<CsdlReferentialConstraint> determineReferentialConstraint(
      final IntermediateJoinColumn intermediateColumn) throws ODataJPAModelException, ODataJPAModelIgnoreException {

    final CsdlReferentialConstraint constraint = new CsdlReferentialConstraint();
    boolean ignore = false;
    IntermediateModelElement p = null;
    p = sourceType.getPropertyByDBField(intermediateColumn.getName());
    if (p != null) {
      if (p.ignore())
        ignore = true;
      constraint.setProperty(p.getExternalName());
      p = targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
      if (p != null) {
        if (p.ignore())
          ignore = true;
        constraint.setReferencedProperty(p.getExternalName());
      } else {
        throw new ODataJPAModelException(REFERENCED_PROPERTY_NOT_FOUND, getInternalName(), intermediateColumn
            .getReferencedColumnName(), targetType.getExternalName());
      }
    } else {
      if (targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName()) != null) {
        LOGGER.trace("Cloud not determine referential constraint for " + this.getType().getName() + "#"
            + getInternalName());
        throw new ODataJPAModelIgnoreException();
      } else
        return Optional.empty();
    }
    if (ignore)
      throw new ODataJPAModelIgnoreException();
    return Optional.of(constraint);
  }

  private void determineReferentialConstraints(final AnnotatedElement annotatedElement) throws ODataJPAModelException {

    final AssociationOverride overwrite = annotatedElement.getAnnotation(AssociationOverride.class);
    if (overwrite != null || joinTable != null)
      return;

    final List<CsdlReferentialConstraint> constraints = edmNaviProperty.getReferentialConstraints();
    constraints.clear();
    boolean ignore = false;
    for (final IntermediateJoinColumn intermediateColumn : joinColumns) {
      try {
        Optional<CsdlReferentialConstraint> constraint = determineReferentialConstraint(intermediateColumn);
        if (!constraint.isPresent())
          constraint = determineReverseReferentialConstraint(intermediateColumn);
        constraints.add(constraint.orElseThrow(
            () -> new ODataJPAModelException(REFERENCED_PROPERTY_NOT_FOUND, getInternalName(), intermediateColumn
                .getReferencedColumnName(), sourceType.getExternalName())));
      } catch (final ODataJPAModelIgnoreException e) {
        ignore = true;
      }
    }
    if (ignore)
      constraints.clear();
  }

  private Optional<CsdlReferentialConstraint> determineReverseReferentialConstraint(
      final IntermediateJoinColumn intermediateColumn) throws ODataJPAModelException, ODataJPAModelIgnoreException {

    final CsdlReferentialConstraint constraint = new CsdlReferentialConstraint();
    boolean ignore = false;
    IntermediateModelElement p = null;
    p = sourceType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
    if (p != null) {
      if (p.ignore())
        ignore = true;
      constraint.setProperty(p.getExternalName());
      p = targetType.getPropertyByDBField(intermediateColumn.getName());
      if (p != null) {
        if (p.ignore())
          ignore = true;
        constraint.setReferencedProperty(p.getExternalName());
      } else {
        // Target not found: system will fallback to default column names, no referential constraint can be given
        LOGGER.trace("Cloud not determine referential constraint for " + this.getType().getName() + "#"
            + getInternalName());
        throw new ODataJPAModelIgnoreException();
      }
    } else {
      return Optional.empty();
    }
    if (ignore)
      throw new ODataJPAModelIgnoreException();
    return Optional.of(constraint);
  }

  private String fillMissingName() throws ODataJPAModelException {
    return ((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
        .getKey().get(0)).getDBFieldName();
  }

  private void fillMissingName(final boolean isSourceOne, final IntermediateJoinColumn intermediateColumn)
      throws ODataJPAModelException {

    final String referencedColumnName = intermediateColumn.getReferencedColumnName();
    final String name = intermediateColumn.getName();

    if (isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
    else if (isSourceOne && (referencedColumnName == null || referencedColumnName.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (referencedColumnName == null || referencedColumnName.isEmpty()))
      intermediateColumn.setName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) targetType)
          .getKey().get(0)).getDBFieldName());
    else if (!isSourceOne && (name == null || name.isEmpty()))
      intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType<?>) sourceType)
          .getKey().get(0)).getDBFieldName());
  }

  private List<IntermediateJoinColumn> invertJoinColumns(final List<IntermediateJoinColumn> joinColumns) {
    final List<IntermediateJoinColumn> invertedJoinColumns = new ArrayList<>(joinColumns.size());
    for (final IntermediateJoinColumn joinColumn : joinColumns) {
      invertedJoinColumns.add(
          new IntermediateJoinColumn(joinColumn.getReferencedColumnName(), joinColumn.getName()));
    }
    return invertedJoinColumns;
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
