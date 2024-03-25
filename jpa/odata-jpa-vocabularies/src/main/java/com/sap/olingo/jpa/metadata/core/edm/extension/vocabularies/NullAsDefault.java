package com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Meta annotation to marks a field of an annotation that the real default value shall be null instead of the given one.
 * This is necessary as null as default is not supported by Java
 */
@Retention(RUNTIME)
public @interface NullAsDefault {

}
