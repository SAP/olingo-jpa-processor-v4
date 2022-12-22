/**
*
*/
package com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * OData core annotation <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L500"><i>SortRestrictions</i></a>:
 * <br>
 * Restrictions on orderby expressions.
 * <p>
 *
 * AppliesTo: EntitySet
 *
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SortRestrictions {
  /**
   * $orderby is supported
   * @return
   */
  boolean sortable() default true;

  /**
   * These properties can only be used for sorting in Ascending order.
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] ascendingOnlyProperties() default {};

  /**
   * These properties can only be used for sorting in Descending order.
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] descendingOnlyProperties() default {};

  /**
   * These structural properties cannot be used in orderby expressions
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonSortableProperties() default {};
}
