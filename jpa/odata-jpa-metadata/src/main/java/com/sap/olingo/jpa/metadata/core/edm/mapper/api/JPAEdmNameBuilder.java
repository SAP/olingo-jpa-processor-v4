/**
 * 
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

/**
 * @author Oliver Grande
 * Created: 15.09.2019
 *
 */
public interface JPAEdmNameBuilder {

  String buildComplexTypeName(EmbeddableType<?> jpaEnbeddedType);

  /**
   * Container names are
   * <a
   * href="http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_SimpleIdentifier">
   * Simple Identifier</a>,
   * so can contain only letters, digits and underscores.
   */
  String buildContainerName();

  default String buildEntitySetName(final CsdlEntityType entityType) {
    return buildEntitySetName(entityType.getName());
  }

  /**
   * The name of an
   * <a
   * href="http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_12.2_The_edm:EntitySet">
   * Entity Set</a> derived from the name of the corresponding entity type.
   * @param entityTypeName
   * @return
   */
  String buildEntitySetName(final String entityTypeName);

  String buildEntityTypeName(final EntityType<?> jpaEntityType);

  /**
   * Convert the internal java class name of an enumeration into the external entity data model name.
   * @param javaEnum
   * @return
   */
  String buildEnumerationTypeName(Class<? extends Enum<?>> javaEnum);

  /**
   * Creates the name of a <a
   * href="http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398035">
   * Navigation Property Binding</a>.
   * @param associationPath
   * @param parent
   * @return
   */
  String buildNaviPropertyBindingName(final JPAAssociationPath associationPath, final JPAAttribute parent);

  /**
   * Converts the name of an JPA association attribute into the name of an EDM navigation property
   * @param jpaAttribute
   * @return
   */
  String buildNaviPropertyName(Attribute<?, ?> jpaAttribute);

  /**
   * Convert the internal name of a java based operation into the external entity data model name.
   * @param internalOperationName
   * @return
   */
  String buildOperationName(String internalOperationName);

  /**
   * Converts the name of an JPA attribute into the name of an EDM property
   * @param jpaAttributeName
   * @return
   */
  String buildPropertyName(String jpaAttributeName);

  String getNamespace();
}