package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes which authorization claim shall be used to filter the annotated attribute. In case multiple attributes of
 * an entity are annotated they are connected via an AND operation.
 * @author Oliver Grande
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EdmProtectedBy {
  /**
   * Name of the authorization claim
   * @return
   */
  String name();

  /**
   * Optional: At complex properties path to the properties that shall be protected by the claim. In case the path is
   * composed, path segments joined together by forward slashes (/)
   * @return
   */
  String[] path() default {};
}
