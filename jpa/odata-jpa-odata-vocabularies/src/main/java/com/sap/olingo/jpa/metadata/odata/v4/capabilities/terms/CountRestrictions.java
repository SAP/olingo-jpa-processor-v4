/**
 *
 */
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.Applicability;
import com.sap.olingo.jpa.metadata.odata.v4.general.Vocabulary;

/**
 *
 * OData core annotation <a href=
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L210"><i>CountRestrictions</i></a>:
 * <br>
 * Restrictions on /$count path suffix and $count=true system query option
 * <p>
 * @author Oliver Grande
 * @since 1.1.1
 * 12.01.2023
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET, Applicability.SINGLETON })
public @interface CountRestrictions {

  /**
   * Entities can be counted (only valid if targeting an entity set).
   */
  boolean countable() default true;

  /**
   * Members of these collection properties cannot be counted.
   * <p>
   * The properties are given as an array of attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonCountableProperties() default {};

  /**
   * Members of these navigation properties cannot be counted
   * <p>
   * The navigation properties are given as an array of attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonCountableNavigationProperties() default {};
}
