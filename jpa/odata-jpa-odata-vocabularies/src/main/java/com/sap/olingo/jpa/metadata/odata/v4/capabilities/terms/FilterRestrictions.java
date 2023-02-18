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
 * OData core annotation
 * <a href=
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L430"><i>FilterRestrictions</i></a>:
 * <br>
 * Restrictions on filter expressions.
 * <p>
 *
 * AppliesTo: EntitySet
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET })
public @interface FilterRestrictions {
  /** $filter is supported */
  boolean filterable() default true;

  /**
   * Optional: These properties must be specified in the $filter clause (properties of derived types are not allowed
   * here)
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] requiredProperties() default {};

  /**
   * Allowed subset of expressions
   * @return
   */
  FilterExpressionRestrictionType[] filterExpressionRestrictions() default {};

  @interface FilterExpressionRestrictionType {
    /**
     * Path to the restricted property
     * <p>
     * The properties are given as an attribute path.
     */
    String property();

    FilterExpressionType allowedExpressions();
  }
}
