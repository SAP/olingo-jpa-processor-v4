package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an attribute as one that is a data stream. Two options are given to describe the content. Either static as
 * string (contentType) or dynamically in another attribute (contentTypeAttribute). Exactly on has to be given.
 * @author Oliver Grande
 *
 */
@Target({ ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EdmMediaStream {
  boolean stream() default true;

  String contentType() default "";

  String contentTypeAttribute() default "";
}
