package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;

/**
 * http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-
 * complete.html#_Toc406397962
 * @author Oliver Grande
 *
 */
class IntermediateNavigationProperty extends IntermediateModelElement implements IntermediateNavigationPropertyAccess,
    JPAAssociationAttribute {

  private final Attribute<?, ?> jpaAttribute;
  private CsdlNavigationProperty edmNaviProperty = null;
  // private final IntermediateStructuredType sourceType;
  private IntermediateStructuredType targetType;
  private final IntermediateSchema schema;
  private final List<JoinColumn> joinColumns = new ArrayList<JoinColumn>();

  IntermediateNavigationProperty(JPAEdmNameBuilder nameBuilder, IntermediateStructuredType parent,
      Attribute<?, ?> jpaAttribute,
      IntermediateSchema schema) {
    super(nameBuilder, intNameBuilder.buildAssociationName(jpaAttribute));
    this.jpaAttribute = jpaAttribute;
    this.schema = schema;
    // this.sourceType = parent;
    buildNaviProperty();

  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmNaviProperty == null) {
      edmNaviProperty = new CsdlNavigationProperty();
      edmNaviProperty.setName(getExternalName());
      edmNaviProperty.setType(nameBuilder.buildFQN(targetType.getExternalName()));
      edmNaviProperty.setCollection(jpaAttribute.isCollection());
      // Optional --> ReleationAnnotation
      if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
        AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
        switch (jpaAttribute.getPersistentAttributeType()) {
        case ONE_TO_MANY:
          // OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
          break;
        case ONE_TO_ONE:
          OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
          edmNaviProperty.setNullable(cardinalityOtO.optional());
          break;
        case MANY_TO_ONE:
          ManyToOne cardinalityMtO = annotatedElement.getAnnotation(ManyToOne.class);
          edmNaviProperty.setNullable(cardinalityMtO.optional());
          break;
        default:
          break;
        }

        JoinColumns columns = annotatedElement.getAnnotation(JoinColumns.class);
        if (columns != null) {
          for (JoinColumn column : columns.value()) {
            joinColumns.add(column);
          }
          // TODO Attribute Nullable
          // edmNaviProperty.setNullable(column.nullable());
        } else {
          JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
          if (column != null) {
            joinColumns.add(column);
          }
        }
      }
      // TODO determine ContainsTarget
      // TODO determine Partner --> mappedBy Attribute
      // TODO determine ReferentialConstraint
      // TODO determine OnDelete
    }

  }

  @Override
      CsdlNavigationProperty getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmNaviProperty;
  }

  private void buildNaviProperty() {
    this.setExternalName(nameBuilder.buildNaviPropertyName(jpaAttribute));
    Class<?> targetClass = null;
    if (jpaAttribute.isCollection()) {
      targetClass = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType();
    } else {
      targetClass = jpaAttribute.getJavaType();
    }
    if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(
          EdmIgnore.class);
      if (jpaIgnore != null) {
        this.setIgnore(true);
      }
    }

    targetType = schema.getEntityType(targetClass);
    postProcessor.processNavigationProperty(this, jpaAttribute.getDeclaringType().getJavaType()
        .getCanonicalName());
  }

  @Override
  public AttributeConverter<?, ?> getConverter() {
    return null;
  }

  @Override
  public JPAStructuredType getStructuredType() {
    return null;
  }

  @Override
  public Class<?> getType() {
    return jpaAttribute.getJavaType();
  }

  @Override
  public boolean isComplex() {
    return false;
  }

  @Override
  public boolean isKey() {
    return false;
  }

  @Override
  public boolean isAssociation() {
    return true;
  }

  @Override
  public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return targetType;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() {
    return null;
  }

  List<JoinColumn> getJoinColumns() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return joinColumns;
  }

  PersistentAttributeType getJoinCardinality() throws ODataJPAModelException {
    return jpaAttribute.getPersistentAttributeType();
  }

  @Override
  public boolean isCollection() {
    return jpaAttribute.isCollection();
  }

  @Override
  public CsdlNavigationProperty getProperty() throws ODataJPAModelException {
    return getEdmItem();
  }
}
