package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.metamodel.EmbeddableType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCache;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.cache.InstanceCacheSupplier;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

/**
 * Complex Types are used to structure Entity Types by grouping properties that belong together. Complex Types can
 * contain of
 * <ul>
 * <li>Properties
 * <li>Navigation Properties
 * </ul>
 * This means that they can contain of primitive, complex, or enumeration type, or a collection of primitive, complex,
 * or enumeration types.
 * <p>
 * <b>Limitation:</b> As of now the attributes BaseType, Abstract and OpenType are not supported. There is also no
 * support for nested complex types
 * <p>
 * Complex Types are generated from JPA Embeddable Types.
 * <p>
 * For details about Complex Type metadata see:
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397985"
 * >OData Version 4.0 Part 3 - 9 Complex Type</a>
 * @author Oliver Grande
 *
 */
final class IntermediateComplexType<T> extends IntermediateStructuredType<T> {
  private static final Log LOGGER = LogFactory.getLog(IntermediateComplexType.class);
  private final InstanceCache<IntermediateStructuredType<? super T>> baseType;

  IntermediateComplexType(final JPAEdmNameBuilder nameBuilder, final EmbeddableType<T> jpaEmbeddable,
      final IntermediateSchema schema) {

    super(nameBuilder, jpaEmbeddable, schema);
    this.setExternalName(nameBuilder.buildComplexTypeName(jpaEmbeddable));
    baseType = new InstanceCacheSupplier<>(this::determineBaseType);
  }

  private IntermediateComplexType(IntermediateComplexType<T> source, List<String> requesterUserGroups)
      throws ODataJPAModelException {
    super(source, requesterUserGroups);
    setExternalName(source.getExternalName());
    baseType = new InstanceCacheFunction<>(this::baseTypeRestricted, source.getBaseType(),
        requesterUserGroups);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <X extends IntermediateModelElement> X asUserGroupRestricted(List<String> userGroups)
      throws ODataJPAModelException {
    return (X) new IntermediateComplexType<>(this, userGroups);
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    getEdmItem();
  }

  @Override
  synchronized CsdlComplexType buildEdmItem() {
    try {
      var edmComplexType = new CsdlComplexType();

      edmComplexType.setName(this.getExternalName());
      edmComplexType.setProperties(extractEdmModelElements(getDeclaredPropertiesMap()));
      edmComplexType.setNavigationProperties(extractEdmModelElements(getDeclaredNavigationPropertiesMap()));
      edmComplexType.setBaseType(determineBaseTypeFqn());
      // TODO Abstract
      // edmComplexType.setAbstract(isAbstract)
      // TODO OpenType
      // edmComplexType.setOpenType(isOpenType)
      if (determineHasStream())
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_EMBEDDED_STREAM,
            internalName);
      checkPropertyConsistency();
      return edmComplexType;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  @Override
  protected synchronized Map<String, IntermediateProperty> buildCompletePropertyMap() {
    try {
      Map<String, IntermediateProperty> result = new HashMap<>();
      result.putAll(buildPropertyList());
      result.putAll(addDescriptionProperty());
      result.putAll(addTransientProperties());
      return result;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAModelInternalException(e);
    }
  }

  @Override
  CsdlComplexType getEdmItem() throws ODataJPAModelException {
    return (CsdlComplexType) super.getEdmItem();
  }

  @Override
  public IntermediateStructuredType<? super T> getBaseType() throws ODataJPAModelException {
    return baseType.get().orElse(null);
  }

  /**
   * Determines if the structured type has a super type that will be part of OData metadata. That is, the method will
   * return null in case the entity has a MappedSuperclass.
   * @return Determined super type or null
   */
  @SuppressWarnings("unchecked")
  public IntermediateStructuredType<? super T> determineBaseType() { // NOSONAR
    final Class<?> superType = jpaManagedType.getJavaType().getSuperclass();
    if (superType != null) {
      final IntermediateStructuredType<? super T> baseComplex = (IntermediateStructuredType<? super T>) schema
          .getComplexType(superType);
      if (baseComplex != null)
        return baseComplex;
      else if (superType != Object.class)
        LOGGER.warn("Embeddable " + jpaManagedType.getJavaType().getName()
            + " is subtype of " + superType.getName() + " but this is not embeddable or shall be ignored");
    }
    return null;
  }

  @Override
  protected Map<String, JPAPath> getBaseTypeResolvedPathMap() throws ODataJPAModelException {
    final IntermediateStructuredType<? super T> superType = getBaseType();
    if (superType != null) {
      return superType.getResolvedPathMap();
    }
    return Map.of();
  }

  @Override
  List<JPAAttribute> getBaseTypeAttributes() throws ODataJPAModelException {
    final IntermediateStructuredType<? super T> baseType = getBaseType();
    if (baseType != null)
      return baseType.getAttributes();
    return List.of();
  }
}
