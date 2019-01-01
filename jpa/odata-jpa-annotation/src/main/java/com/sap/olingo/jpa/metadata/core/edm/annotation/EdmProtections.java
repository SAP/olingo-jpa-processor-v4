package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to combine either multiple claims for a simple attribute, if both are present they will be combined with OR,
 * or to be able to protect multiple attributes at a complex attribute.
 * @author Oliver Grande
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface EdmProtections {
  public abstract EdmProtectedBy[] value();
}
