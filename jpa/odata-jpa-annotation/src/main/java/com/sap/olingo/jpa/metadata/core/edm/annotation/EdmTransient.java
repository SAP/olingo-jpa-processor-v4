/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation marks attributes that are transient, but shall be part of the Edm.<br>
 * Please note that the attribute needs the corresponding JPA annotation {@link Transient} as well.
 * @author Oliver Grande
 * Created: 25.11.2019
 */

@Target({ FIELD })
@Retention(value = RUNTIME)
public @interface EdmTransient {
  /**
   * Optional: An array of path to attributes that need to be present to build the annotated one. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   * </p>
   * The correctness
   * of the path is check when the entity type is build.
   */
  String[] requiredAttributes() default {};

  /**
   * A calculator is an implementation of {@link EdmTransientPropertyCalculator}. It provides the transient property or
   * property collection.
   * @return
   */
  Class<? extends EdmTransientPropertyCalculator<?>> calculator();
}
