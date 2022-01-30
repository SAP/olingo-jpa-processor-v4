package com.sap.olingo.jpa.metadata.odata.v4.core.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Target;

/**
 * OData core annotation <i>Immutable</i> <br>
 * A value for this non-key property can be provided on insert and remains unchanged on update
 * @author Oliver Grande
 *
 */
@Target(FIELD)
public @interface Immutable {
  boolean value() default true;
}
