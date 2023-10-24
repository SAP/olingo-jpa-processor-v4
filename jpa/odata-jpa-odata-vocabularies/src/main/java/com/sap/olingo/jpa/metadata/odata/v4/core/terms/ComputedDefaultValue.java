package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <i>ComputedDefaultValue</i>: <br>
 * A value for this property can be provided by the client on insert and update. If no value is provided on insert, a
 * non-static default value is generated.
 * <p>
 *
 * AppliesTo: Property
 * @author Oliver Grande
 *
 */
@Target(FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Vocabulary(alias = "Core", appliesTo = { Applicability.PROPERTY })
public @interface ComputedDefaultValue {
  boolean value() default true;
}
