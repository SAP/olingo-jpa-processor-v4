package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397974"
 * >OData Version 4.0 Part 3 - 8 Entity Type</a>
 * @author Oliver Grande
 *
 */
class IntermediateEntityType extends IntermediateStructuredType implements JPAEntityType {
  CsdlEntityType edmEntityType;

  IntermediateEntityType(final JPAEdmNameBuilder nameBuilder, final EntityType<?> et, final IntermediateSchema schema)
      throws ODataJPAModelException {
    super(nameBuilder, et, schema);
    this.setExternalName(nameBuilder.buildEntityTypeName(et));
    final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaManagedType.getJavaType()).getAnnotation(
        EdmIgnore.class);
    if (jpaIgnore != null) {
      this.setIgnore(true);
    }
  }

  @Override
  public List<? extends JPAAttribute> getKey() throws ODataJPAModelException {
    lazyBuildEdmItem();
    final List<JPAAttribute> key = new ArrayList<JPAAttribute>();

    for (final String internalName : this.declaredPropertiesList.keySet()) {
      final JPAAttribute attribute = this.declaredPropertiesList.get(internalName);
      if (attribute.isKey()) {
        if (attribute.isComplex()) {
          key.addAll(((IntermediateEmbeddedIdProperty) attribute).getStructuredType().getAttributes());
        } else
          key.add(attribute);
      }
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null) {
      key.addAll(((IntermediateEntityType) baseType).getKey());
    }
    return key;
  }

  @Override
  public List<JPAPath> getKeyPath() throws ODataJPAModelException {
    lazyBuildEdmItem();

    List<JPAPath> result = new ArrayList<JPAPath>();
    for (final String internalName : this.declaredPropertiesList.keySet()) {
      final JPAAttribute attribute = this.declaredPropertiesList.get(internalName);
      if (attribute instanceof IntermediateEmbeddedIdProperty) {
        result.add(intermediatePathMap.get(attribute.getExternalName()));
      } else if (attribute.isKey())
        result.add(resolvedPathMap.get(attribute.getExternalName()));
    }
    final IntermediateStructuredType baseType = getBaseType();
    if (baseType != null) {
      result.addAll(((IntermediateEntityType) baseType).getKeyPath());
    }
    return result;
  }

  @Override
  public Class<?> getKeyType() {
    if (jpaManagedType instanceof IdentifiableType<?>)
      return ((IdentifiableType<?>) jpaManagedType).getIdType().getJavaType();
    else
      return null;
  }

  @Override
  public List<JPAPath> getSearchablePath() throws ODataJPAModelException {
    final List<JPAPath> allPath = getPathList();
    final List<JPAPath> searchablePath = new ArrayList<JPAPath>();
    for (JPAPath p : allPath) {
      if (p.getLeaf().isSearchable())
        searchablePath.add(p);
    }
    return searchablePath;
  }

  @Override
  public String getTableName() {
    final AnnotatedElement a = jpaManagedType.getJavaType();
    Table t = null;

    if (a != null)
      t = a.getAnnotation(Table.class);

    return (t == null) ? jpaManagedType.getJavaType().getName().toUpperCase(Locale.ENGLISH)
        : t.name();
  }

  @Override
  public List<JPAPath> searchChildPath(final JPAPath selectItemPath) {
    final List<JPAPath> result = new ArrayList<JPAPath>();
    for (final String pathName : this.resolvedPathMap.keySet()) {
      final JPAPath p = resolvedPathMap.get(pathName);
      if (!p.ignore() && p.getAlias().startsWith(selectItemPath.getAlias()))
        result.add(p);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <T> List<?> extractEdmModelElements(Map<String, ?> mappingBuffer) throws ODataJPAModelException {
    final List<T> extractionTarget = new ArrayList<T>();
    for (final String externalName : mappingBuffer.keySet()) {
      if (!((IntermediateModelElement) mappingBuffer.get(externalName)).ignore()) {
        if (mappingBuffer.get(externalName) instanceof IntermediateEmbeddedIdProperty) {
          extractionTarget.addAll((Collection<? extends T>) resolveEmbeddedId(
              (IntermediateEmbeddedIdProperty) mappingBuffer.get(externalName)));
        } else
          extractionTarget.add((T) ((IntermediateModelElement) mappingBuffer.get(externalName)).getEdmItem());
      }
    }
    return returnNullIfEmpty(extractionTarget);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEntityType == null) {
      // TODO store @Version to fill ETag Header
      buildPropertyList();
      edmEntityType = new CsdlEntityType();
      edmEntityType.setName(getExternalName());
      edmEntityType.setProperties((List<CsdlProperty>) extractEdmModelElements(declaredPropertiesList));
      edmEntityType.setNavigationProperties((List<CsdlNavigationProperty>) extractEdmModelElements(
          declaredNaviPropertiesList));
      edmEntityType.setKey(extractEdmKeyElements(declaredPropertiesList));
      // TODO check: An abstract entity type MUST NOT inherit from a non-abstract entity type.
      edmEntityType.setAbstract(determineAbstract());
      edmEntityType.setBaseType(determineBaseType());
      // TODO determine OpenType
      // TODO determine HasStream
    }
  }

  boolean determineAbstract() {
    final int modifiers = jpaManagedType.getJavaType().getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  /**
   * Creates the key of an entity. In case the POJP is declared with an embedded ID the key fields get resolved, so that
   * they occur as separate properties within the metadata document
   * 
   * @param propertyList
   * @return
   * @throws ODataJPAModelException
   */
  List<CsdlPropertyRef> extractEdmKeyElements(final Map<String, IntermediateProperty> propertyList)
      throws ODataJPAModelException {

    final List<CsdlPropertyRef> keyList = new ArrayList<CsdlPropertyRef>();
    for (final String internalName : propertyList.keySet()) {
      if (propertyList.get(internalName).isKey()) {
        if (propertyList.get(internalName).isComplex()) {
          List<JPAAttribute> idAttributes = ((IntermediateComplexType) propertyList.get(internalName)
              .getStructuredType())
                  .getAttributes();
          for (JPAAttribute idAttribute : idAttributes) {
            final CsdlPropertyRef key = new CsdlPropertyRef();
            key.setName(idAttribute.getExternalName());
            // TODO setAlias
            keyList.add(key);
          }
        } else {
          final CsdlPropertyRef key = new CsdlPropertyRef();
          key.setName(propertyList.get(internalName).getExternalName());
          // TODO setAlias
          keyList.add(key);
        }
      }
    }
    return returnNullIfEmpty(keyList);
  }

  @Override
      CsdlEntityType getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmEntityType;
  }

  private <T> List<?> resolveEmbeddedId(IntermediateEmbeddedIdProperty embeddedId) throws ODataJPAModelException {
    return ((IntermediateComplexType) embeddedId.getStructuredType()).getEdmItem().getProperties();
  }

}
