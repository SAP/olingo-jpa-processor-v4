package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes which authorization claim shall be used to filter the annotated attribute. In case multiple attributes of
 * an entity are annotated they are connected via an AND operation. This annotation is respected also if the attribute
 * is marked to be ignored.
 * @author Oliver Grande
 *
 */
@Repeatable(EdmProtections.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface EdmProtectedBy {
  /**
   * Name of the authorization claim
   * @return
   */
  String name();

  /**
   * Optional: At embedded attributes the path to the attribute that shall be protected by the claim. In case the path
   * is composed, path segments joined together by forward slashes (/).</p> The correctness of the pass is check late
   * during request processing.
   * @return
   */
  String path() default "";

  /**
   * Optional: In case the protected attributes is of type string also wildcards are supported. '*' and '%' representing
   * zero or more characters and '+' as well as '_' for a single character.
   */
  boolean wildcardSupported() default true;
}
