package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an attribute as searchable
 * @author Oliver Grande
 *
 */
@Target({ ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EdmSearchable {

}
