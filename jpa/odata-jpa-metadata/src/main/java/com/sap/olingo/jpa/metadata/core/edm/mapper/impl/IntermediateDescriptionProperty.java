package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation.valueAssignment;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

final class IntermediateDescriptionProperty extends IntermediateSimpleProperty implements JPADescriptionAttribute,
    JPAAssociationAttribute {
  private IntermediateSimpleProperty descriptionProperty;
  private String localeAttribute;
  private final IntermediateStructuredType sourceType;
  private JPAStructuredType targetEntity;
  private HashMap<JPAPath, String> fixedValues;
  private JPAPath localFieldPath;
  private Optional<JPAAssociationPath> assoziationPath;

  IntermediateDescriptionProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateStructuredType parent, final IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
    this.sourceType = parent;
    this.assoziationPath = Optional.empty();
  }

  @Override
  public JPAAttribute getDescriptionAttribute() {
    return descriptionProperty;
  }

  @Override
  public Map<JPAPath, String> getFixedValueAssignment() {
    return fixedValues;
  }

  @Override
  public JPAPath getLocaleFieldName() {
    return localFieldPath;
  }

  /**
   * @return Type of description attribute
   */
  @Override
  public Class<?> getType() {
    return descriptionProperty.getType();
  }

  @Override
  public boolean isAssociation() {
    return true;
  }

  @Override
  public boolean isLocationJoin() {
    return !localeAttribute.isEmpty();
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    Member jpaMember = jpaAttribute.getJavaMember();
    String languageAttribute;

    if (this.edmProperty == null) {
      super.lazyBuildEdmItem();
      if (jpaMember instanceof AnnotatedElement) {
        final EdmDescriptionAssoziation assozation = ((AnnotatedElement) jpaMember).getAnnotation(
            EdmDescriptionAssoziation.class);
        if (assozation != null) {
          // determine generic type of a collection in case of an OneToMany association
          if (jpaMember instanceof Field) {
            final Field jpaField = (Field) jpaMember;
            final ParameterizedType jpaTargetEntityType = (ParameterizedType) jpaField.getGenericType();
            if (jpaTargetEntityType != null)
              targetEntity = schema.getEntityType((Class<?>) jpaTargetEntityType.getActualTypeArguments()[0]);
            else
              targetEntity = schema.getEntityType(jpaAttribute.getJavaType());
          } else {
            targetEntity = schema.getEntityType(jpaAttribute.getJavaType());
          }
          descriptionProperty = (IntermediateSimpleProperty) targetEntity.getAttribute(assozation
              .descriptionAttribute()).orElseThrow(() ->
          // The attribute %2$s has not been found at entity %1$s
          new ODataJPAModelException(MessageKeys.INVALID_DESCRIPTION_PROPERTY, targetEntity.getInternalName(),
              assozation.descriptionAttribute()));
          languageAttribute = assozation.languageAttribute();
          localeAttribute = assozation.localeAttribute();

          if (emptyString(languageAttribute) && emptyString(localeAttribute) ||
              !languageAttribute.isEmpty() && !localeAttribute.isEmpty())
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_LOCALE_FIELD_MISSING,
                targetEntity.getInternalName(), this.internalName);
          if (!descriptionProperty.getType().equals(String.class))
            throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_FIELD_WRONG_TYPE,
                targetEntity.getInternalName(), this.internalName);

          edmProperty.setType(JPATypeConverter.convertToEdmSimpleType(descriptionProperty.getType())
              .getFullQualifiedName());
          edmProperty.setMaxLength(descriptionProperty.getEdmItem().getMaxLength());

          fixedValues = convertFixedValues(assozation.valueAssignments());
          localFieldPath = convertAttributeToPath(!languageAttribute.isEmpty() ? languageAttribute : localeAttribute);
        } else {
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.DESCRIPTION_ANNOTATION_MISSING,
              sourceType.getInternalName(), this.internalName);
        }
      }
    }
  }

  private JPAPath convertAttributeToPath(final String attribute) throws ODataJPAModelException {
    final String[] pathItems = attribute.split(JPAPath.PATH_SEPARATOR);
    if (pathItems.length > 1) {
      final List<JPAElement> targetPath = new ArrayList<>();
      IntermediateSimpleProperty nextHop = (IntermediateSimpleProperty) targetEntity.getAttribute(pathItems[0])
          .orElseThrow(() -> new ODataJPAModelException(MessageKeys.PATH_ELEMENT_NOT_FOUND, pathItems[0], attribute));
      targetPath.add(nextHop);
      for (int i = 1; i < pathItems.length; i++) {
        if (nextHop.isComplex()) {
          nextHop = (IntermediateSimpleProperty) nextHop.getStructuredType().getAttribute(pathItems[i])
              .orElseThrow(() -> new ODataJPAModelException(MessageKeys.PATH_ELEMENT_NOT_FOUND, pathItems[0],
                  attribute));
          targetPath.add(nextHop);
        }
      }
      return new JPAPathImpl(nextHop.getExternalName(), nextHop.getDBFieldName(), targetPath);
    } else {
      final IntermediateSimpleProperty p = (IntermediateSimpleProperty) targetEntity.getAttribute(attribute)
          .orElseThrow(() -> new ODataJPAModelException(MessageKeys.PATH_ELEMENT_NOT_FOUND, pathItems[0], attribute));
      return new JPAPathImpl(p.getExternalName(), p.getDBFieldName(), p);
    }
  }

  private HashMap<JPAPath, String> convertFixedValues(final valueAssignment[] valueAssignments)
      throws ODataJPAModelException {
    final HashMap<JPAPath, String> result = new HashMap<>();
    for (final EdmDescriptionAssoziation.valueAssignment value : valueAssignments) {
      result.put(convertAttributeToPath(value.attribute()), value.value());
    }
    return result;
  }

  @Override
  public JPAAssociationAttribute asAssociationAttribute() {
    return this;
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    return targetEntity;
  }

  @Override
  public JPAAssociationAttribute getPartner() {
    return null;
  }

  @Override
  public JPAAssociationPath getPath() throws ODataJPAModelException {
    return assoziationPath.orElseGet(() -> {
      assoziationPath = Optional.of(new AssoziationPath());
      return assoziationPath.get();
    });
  }

  private class AssoziationPath implements JPAAssociationPath {
    private List<IntermediateJoinColumn> joinColumns = null;

    @Override
    public String getAlias() {
      return null;
    }

    @Override
    public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
      if (joinColumns == null)
        joinColumns = buildJoinColumnsFromAnnotations(true, (AnnotatedElement) jpaAttribute.getJavaMember());
      final List<JPAOnConditionItem> result = new ArrayList<>();
      for (final IntermediateJoinColumn column : this.joinColumns) {
        result.add(new JPAOnConditionItemImpl(
            sourceType.getPathByDBField(column.getReferencedColumnName()),
            ((IntermediateStructuredType) targetEntity).getPathByDBField(column.getName())));
      }
      return result;
    }

    @Override
    public List<JPAPath> getLeftColumnsList() throws ODataJPAModelException {
      return Collections.emptyList();
    }

    @Override
    public List<JPAPath> getRightColumnsList() throws ODataJPAModelException {
      return Collections.emptyList();
    }

    @Override
    public JPAAssociationAttribute getLeaf() {
      return null;
    }

    @Override
    public List<JPAElement> getPath() {
      return Collections.emptyList();
    }

    @Override
    public JPAStructuredType getTargetType() {
      return targetEntity;
    }

    @Override
    public JPAStructuredType getSourceType() {
      return sourceType;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

    @Override
    public JPAAssociationAttribute getPartner() {
      return null;
    }

    @Override
    public JPAJoinTable getJoinTable() {
      return null;
    }

    @Override
    public List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException {
      return Collections.emptyList();
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
          result.add(intermediateColumn);
        }
      }
      return result;
    }

    private void fillMissingName(final boolean isSourceOne, final IntermediateJoinColumn intermediateColumn)
        throws ODataJPAModelException {

      final String refColumnName = intermediateColumn.getReferencedColumnName();
      final String name = intermediateColumn.getName();

      if (isSourceOne && (emptyString(refColumnName)))
        intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) sourceType)
            .getKey().get(0)).getDBFieldName());
      else if (isSourceOne && (emptyString(name)))
        intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) targetEntity)
            .getKey().get(0)).getDBFieldName());
      else if (!isSourceOne && (emptyString(refColumnName)))
        intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) targetEntity)
            .getKey().get(0)).getDBFieldName());
      else if (!isSourceOne && (emptyString(name)))
        intermediateColumn.setReferencedColumnName(((IntermediateSimpleProperty) ((IntermediateEntityType) sourceType)
            .getKey().get(0)).getDBFieldName());
    }

    @Override
    public boolean hasJoinTable() {
      return false;
    }
  }
}
