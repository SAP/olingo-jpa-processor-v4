/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.general;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
/**
 * @author Oliver Grande
 * @since 1.1.1
 * 02.01.2023
 */
public @interface Vocabulary {
  /**
   * Alias given in the vocabulary definition. E.g. <em>Capabilities</em> for
   * <a href=
   * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml">Org.OData.Capabilities.V1</a>
   */
  String alias();

  /**
   * Supported applicability. This could be a subset of the defined applicability.
   */
  Applicability[] appliesTo();
}
