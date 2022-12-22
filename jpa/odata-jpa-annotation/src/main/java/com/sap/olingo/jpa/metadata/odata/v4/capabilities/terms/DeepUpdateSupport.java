/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
/**
 * OData core annotation <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L774"><i>DeepUpdateSupport</i></a>:
 * <br>
 * Deep Update Support of the annotated resource (the whole service, an entity set, or a collection-valued resource)
 * <p>
 *
 * AppliesTo: EntitySet
 * @author Oliver Grande
 * Created: 29.04.2021
 *
 */
public @interface DeepUpdateSupport {
  /**
   * Annotation target supports deep updates
   */
  boolean supported() default true;

  /**
   * Annotation target supports accepting and returning nested entities annotated with the `Core.ContentID` instance
   * annotation.
   */
  boolean contentIDSupported() default true;
}
