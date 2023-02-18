/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.core.terms;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <a href=
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Core.V1.xml#L436"><i>AutoExpand</i></a>:
 * <br>
 * The service will automatically expand this navigation property even if not requested with $expand. AutoExand at
 * stream properties or media stream of this media entity is not supported.
 * <p>
 *
 * AppliesTo: NavigationProperty
 * @author Oliver Grande
 *
 */

@Retention(RUNTIME)
@Target({ TYPE, FIELD })
@Vocabulary(alias = "Core", appliesTo = { Applicability.NAVIGATION_PROPERTY })
public @interface AutoExpand {
  boolean value() default true;
}
