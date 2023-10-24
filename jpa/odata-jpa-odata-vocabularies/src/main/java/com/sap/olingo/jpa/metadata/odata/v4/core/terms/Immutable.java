package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <i>Immutable</i>: <br>
 * A value for this non-key property can be provided on insert and remains unchanged on update.
 * <p>
 *
 * AppliesTo: Property
 * @author Oliver Grande
 *
 */
@Target(FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Vocabulary(alias = "Core", appliesTo = { Applicability.PROPERTY })
public @interface Immutable {
  boolean value() default true;
}
