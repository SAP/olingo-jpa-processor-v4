/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.odata.v4.general.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L424"><i>FilterFunctions</i></a>:
 * <br>
 * List of functions and operators supported in filter expressions. If not specified, null, or empty, all functions and
 * operators may be attempted.
 * <p>
 *
 * AppliesTo: EntitySet
 * @author Oliver Grande
 * Created: 28.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET })
public @interface FilterFunctions {
  String[] value() default {};
}
