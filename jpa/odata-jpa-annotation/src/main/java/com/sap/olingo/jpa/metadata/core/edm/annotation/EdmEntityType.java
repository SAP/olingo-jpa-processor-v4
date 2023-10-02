/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation.AS_ENTITY_SET;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes the characteristics of the generated OData artifact of the annotated type. In case this annotation is
 * missing, it is assumed that the type represents an Entity Set.
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)

public @interface EdmEntityType {
  /**
   * Indicates what the type represents. This could be an Entity Set, an Entity Type or a (nullable) Singleton.
   */
  EdmTopLevelElementRepresentation as() default AS_ENTITY_SET;

  /**
   * Query Extension Provider provides a set to methods to extend or influence the generated query. This may be from
   * interest in case a subset of records shall be represented by an Entity Set or Singleton and it is not possible to
   * use a database view.
   * <p>
   *
   * Query Extension Provider are inherited. One that is given at a sub type overrides one that is given at the super
   * type or base type.
   */
  Class<? extends EdmQueryExtensionProvider> extensionProvider() default EdmQueryExtensionProvider.class;
}
