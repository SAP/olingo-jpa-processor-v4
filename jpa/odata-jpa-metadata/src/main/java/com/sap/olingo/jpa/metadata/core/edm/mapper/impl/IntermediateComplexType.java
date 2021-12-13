package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import javax.persistence.metamodel.EmbeddableType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Complex Types are used to structure Entity Types by grouping properties that belong together. Complex Types can
 * contain of
 * <ul> <li> Properties <li> Navigation Properties</ul>
 * This means that they can contain of primitive, complex, or enumeration type, or a collection of primitive, complex,
 * or enumeration types.<p>
 * <b>Limitation:</b> As of now the attributes BaseType, Abstract and OpenType are not supported. There is also no
 * support for nested complex types <p>
 * Complex Types are generated from JPA Embeddable Types.
 * <p>For details about Complex Type metadata see:
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397985"
 * >OData Version 4.0 Part 3 - 9 Complex Type</a>
 * @author Oliver Grande
 *
 */
final class IntermediateComplexType<T> extends IntermediateStructuredType<T> {
  private static final Log LOGGER = LogFactory.getLog(IntermediateComplexType.class);

  IntermediateComplexType(final JPAEdmNameBuilder nameBuilder, final EmbeddableType<T> jpaEmbeddable,
      final IntermediateSchema schema) {

    super(nameBuilder, jpaEmbeddable, schema);
    this.setExternalName(nameBuilder.buildComplexTypeName(jpaEmbeddable));

  }

  @SuppressWarnings("unchecked")
  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      buildPropertyList();
      buildNaviPropertyList();
      addTransientProperties();
      edmStructuralType = new CsdlComplexType();

      edmStructuralType.setName(this.getExternalName());
      edmStructuralType.setProperties((List<CsdlProperty>) extractEdmModelElements(declaredPropertiesList));
      edmStructuralType.setNavigationProperties((List<CsdlNavigationProperty>) extractEdmModelElements(
          declaredNaviPropertiesList));
      edmStructuralType.setBaseType(determineBaseType());
      // TODO Abstract
      // edmComplexType.setAbstract(isAbstract)
      // TODO OpenType
      // edmComplexType.setOpenType(isOpenType)
      if (determineHasStream()) {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_EMBEDDED_STREAM,
            internalName);
      }
    }
  }

  @Override
  CsdlComplexType getEdmItem() throws ODataJPAModelException {
    if (edmStructuralType == null) {
      lazyBuildEdmItem();
    }
    return (CsdlComplexType) edmStructuralType;
  }

  @Override
  protected IntermediateStructuredType<? super T> getBaseType() {
    final Class<?> baseType = jpaManagedType.getJavaType().getSuperclass();
    if (baseType != null) {
      @SuppressWarnings("unchecked")
      final IntermediateStructuredType<? super T> baseComplex = (IntermediateStructuredType<? super T>) schema
          .getComplexType(baseType);
      if (baseComplex != null)
        return baseComplex;
      else if (baseType != Object.class)
        LOGGER.warn("Embeddable " + jpaManagedType.getJavaType().getName()
            + " is subtype of " + baseType.getName() + " but this is not embeddable or shall be ignored");
    }
    return null;
  }
}
