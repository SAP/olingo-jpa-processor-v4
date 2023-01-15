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
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L600"><i>InsertRestrictions</i></a>:
 * <br>
 * Restrictions on insert operations.
 * <p>
 *
 * AppliesTo: EntitySet, EntityType
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET })
public @interface InsertRestrictions {
  /**
   * If <b>true</b>: entities can be inserted. Default: <b>false</b>
   * @return
   */
  boolean insertable() default false;

  // https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html#sec_PathExpressions
  /**
   * These structural attributes cannot be specified on insert
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonInsertableProperties() default {};

  /**
   * These navigation attributes do not allow deep inserts
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] nonInsertableNavigationProperties() default {};

  /**
   * These structural attributes must be specified on insert
   * <p>
   * The properties are given as an array attributes path. In case the path
   * is composed, path segments joined together by forward slashes (/) e.g <i>address/cityName</i>.
   */
  String[] requiredProperties() default {};

  /**
   * The maximum number of navigation properties that can be traversed when addressing the collection to insert into. A
   * value of -1 indicates there is no restriction..
   */
  int maxLevels() default -1;

//  <Property Name="TypecastSegmentSupported" Type="Edm.Boolean" Nullable="false" DefaultValue="true">
//    <Annotation Term="Core.Description" String="Entities of a specific derived type can be created by specifying a type-cast segment" />
//  </Property>
//  <Property Name="Permissions" Type="Collection(Capabilities.PermissionType)" Nullable="true">
//    <Annotation Term="Core.Description" String="Required permissions. One of the specified sets of scopes is required to perform the insert." />
//  </Property>
//  <Property Name="QueryOptions" Type="Capabilities.ModificationQueryOptionsType" Nullable="true">
//    <Annotation Term="Core.Description" String="Support for query options with insert requests" />
//  </Property>
//  <Property Name="CustomHeaders" Type="Collection(Capabilities.CustomParameter)" Nullable="false">
//    <Annotation Term="Core.Description" String="Supported or required custom headers" />
//  </Property>
//  <Property Name="CustomQueryOptions" Type="Collection(Capabilities.CustomParameter)" Nullable="false">
//    <Annotation Term="Core.Description" String="Supported or required custom query options" />
//  </Property>

  /**
   * A brief description of the request.
   * <p>
   * Currently only one language supported.
   */
  String description() default "";

  /**
   * A lengthy description of the request.
   * Currently only one language supported.
   */
  String longDescription() default "";
}
