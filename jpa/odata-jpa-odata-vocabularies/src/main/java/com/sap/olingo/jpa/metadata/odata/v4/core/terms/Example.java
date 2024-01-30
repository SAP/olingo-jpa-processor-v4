package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <i>Example</i>: <br>
 * Example for an instance of the annotated model element. The value of Core.Example is a record/object containing the
 * example value and/or annotation examples. <br>
 * An example can have different flavors. It could be a PrimitiveExampleValue, a ComplexExampleValue, an
 * EntityExampleValue or an ExternalExampleValue. Out of those only the last one is supported by this java annotation.
 * <p>
 * Example:<br>
 * <code>
 * description = "External example"
 * externalValue = "https://services.odata.org/TripPinRESTierService/(S(5fjoyrzpnvzrrvmxzzq25i4q))/Me"
 * </code>
 * <p>
 *
 * AppliesTo: EntityType, Property, NavigationProperty
 * @author Oliver Grande
 *
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
@Vocabulary(alias = "Core", appliesTo = { Applicability.ENTITY_TYPE, Applicability.PROPERTY,
    Applicability.NAVIGATION_PROPERTY })
public @interface Example {

  /**
   * Description of the example value
   */
  String description() default "";

  /**
   * Url reference to the value in its literal format
   */
  String externalValue();

}
