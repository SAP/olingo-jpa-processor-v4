/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 * OData core annotation <a href=
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L677"><i>DeepInsertSupport</i></a>:
 * <br>
 * Deep Insert Support of the annotated resource (the whole service, an entity set, or a collection-valued resource)
 * <p>
 *
 * AppliesTo: EntitySet
 *
 * @author Oliver Grande
 * Created: 01.05.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET })
public @interface DeepInsertSupport {
  /**
   * Annotation target supports deep inserts.
   */
  boolean supported() default true;

  /**
   * Annotation target supports accepting and returning nested entities annotated with the `Core.ContentID` instance
   * annotation.
   */
  boolean contentIDSupported() default true;
}
