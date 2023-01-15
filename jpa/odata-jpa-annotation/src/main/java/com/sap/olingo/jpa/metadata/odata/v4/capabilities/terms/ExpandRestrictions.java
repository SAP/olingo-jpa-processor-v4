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
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L524"><i>ExpandRestrictions</i></a>:
 * Restrictions on expand expressions.
 * <p>
 *
 * AppliesTo: EntitySet Singleton
 * @author Oliver Grande
 * Created: 29.12.2022
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET, Applicability.SINGLETON })
public @interface ExpandRestrictions {
  /**
   * $expand is supported
   */
  boolean expandable() default true;

  /**
   * These properties cannot be used in expand expressions.
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonExpandableProperties() default {};

// $expand not supported
//<Property Name="StreamsExpandable" Type="Edm.Boolean" Nullable="false" DefaultValue="false">
//  <Annotation Term="Core.Description" String="$expand is supported for stream properties and media streams" />
//</Property>

//<Property Name="NonExpandableStreamProperties" Type="Collection(Edm.PropertyPath)" Nullable="false">
//  <Annotation Term="Core.Description" String="These stream properties cannot be used in expand expressions" />
//  <Annotation Term="Core.RequiresType" String="Edm.Stream" />
//</Property>
  /**
   * The maximum number of levels that can be expanded in a expand expression. A value of -1 indicates there is no
   * restriction.
   */
  int maxLevels() default -1;
}
