/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import javax.annotation.Nonnull;

import jakarta.persistence.Column;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

/**
 * A name builder creates, based on information from the JPA entity model names, the names of the corresponding element
 * of the OData entity data model (EDM)
 * @author Oliver Grande
 * Created: 15.09.2019
 *
 */
public interface JPAEdmNameBuilder {
  static final String DB_FIELD_NAME_PATTERN = "\"&1\"";

  /**
   *
   * @param jpaEmbeddedType
   * @return
   */
  @Nonnull
  String buildComplexTypeName(final EmbeddableType<?> jpaEmbeddedType);

  /**
   * Container names are <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_SimpleIdentifier">
   * Simple Identifier</a>,
   * so can contain only letters, digits and underscores.
   * @return non empty unique name of an Entity Set
   */
  @Nonnull
  String buildContainerName();

  /**
   * Create a name of an <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_12.2_The_edm:EntitySet">
   * Entity Set</a> derived from the name of the corresponding entity type.
   * @param entityTypeName
   * @return non empty unique name of an Entity Set
   */
  @Nonnull
  String buildEntitySetName(final String entityTypeName);

  default String buildEntitySetName(final CsdlEntityType entityType) {
    return buildEntitySetName(entityType.getName());
  }

  /**
   * Create a name of an <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398032">
   * Singleton</a> derived from the name of the corresponding entity type.
   * @param entityTypeName
   * @return non empty unique name of a Singleton
   */
  @Nonnull
  default String buildSingletonName(final String entityTypeName) {
    return entityTypeName;
  }

  default String buildSingletonName(final CsdlEntityType entityType) {
    return buildSingletonName(entityType.getName());
  }

  /**
   * Creates the name of an <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397976">Entity
   * Type</a> derived from JPA Entity Type.
   * @param jpaEntityType
   * @return non empty unique name of an Entity Type
   */
  @Nonnull
  String buildEntityTypeName(final EntityType<?> jpaEntityType);

  /**
   * Converts the internal java class name of an enumeration into the external entity data model <a
   * href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397991">
   * Enumeration Type</a> name.
   * @param javaEnum
   * @return non empty unique name of an Enumeration
   */
  @Nonnull
  String buildEnumerationTypeName(final Class<? extends Enum<?>> javaEnum);

  /**
   * Converts the name of an JPA association attribute into the name of an EDM navigation property
   * @param jpaAttribute
   * @return non empty unique name of a Navigation Property
   */
  @Nonnull
  String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute);

  /**
   * Convert the internal name of a java based operation into the external entity data model name.
   * @param internalOperationName
   * @return non empty unique name of an Operation (Function or Action)
   */
  @Nonnull
  String buildOperationName(final String internalOperationName);

  /**
   * Converts the name of an JPA attribute into the name of an EDM property
   * @param jpaAttributeName
   * @return non empty unique name of a property
   */
  @Nonnull
  String buildPropertyName(final String jpaAttributeName);

  /**
   * @return name space to a schema
   */
  @Nonnull
  String getNamespace();

  /**
   * Build the name of the database column in case it is not provided by the annotation {@link Column}.
<<<<<<< HEAD
   * The Default converts a field name by adding quotation marks. E.g.: <br>
   * id -> "id"
   * <p>
   * In case you want to use the field name converted into the default database representation, e.g. all upper case,
   * the you can just return the field name:
   *
   * <pre>
   * {@code
   * return jpaFieldName
=======
   * The Default returns the field name, so it will be converted later into the database default representation.
   * <p>
   * In case the field name should be used as such (old default) you need to surround the field name by quotation marks:
   *
   * <pre>
   * {@code
   * final var stringBuilder = new StringBuilder(DB_FIELD_NAME_PATTERN);
   * stringBuilder.replace(1, 3, jpaFieldName);
   * return stringBuilder.toString();
>>>>>>> jpa-processor/master
   * }
   * </pre>
   *
   *
<<<<<<< HEAD
   * If you would use (upper case) snake case for column names, the implementation could look like this:
=======
   * If you use (upper case) snake case for column names, the implementation could look like this:
>>>>>>> jpa-processor/master
   *
   * <pre>
   * {@code
   * int start = 0;
   * final List<String> splits = new ArrayList<>();
   * final var chars = jpaFieldName.toCharArray();
   * for (int i = 0; i < chars.length; i++) {
   *   if (Character.isUpperCase(chars[i])) {
   *     splits.add(String.copyValueOf(chars, start, i - start));
   *     start = i;
   *   }
   * }
   * if (start < chars.length)
   *   splits.add(String.copyValueOf(chars, start, chars.length - start));
   *
   * return splits.stream()
   *     .map(String::toUpperCase)
   *     .collect(Collectors.joining("_"));
   * }
   * </pre>
   *
   * @param jpaFieldName Name of the field in the entity
   * @return column name
   */
  @Nonnull
  default String buildColumnName(final String jpaFieldName) {
    return jpaFieldName;
  }
}