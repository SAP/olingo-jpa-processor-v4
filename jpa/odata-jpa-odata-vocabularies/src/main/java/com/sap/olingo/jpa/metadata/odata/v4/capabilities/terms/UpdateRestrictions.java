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
 * OData core annotation <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L690"><i>UpdateRestrictions</i></a>:
 * <br>
 * Restrictions on update operations.
 * <p>
 *
 * AppliesTo: EntitySet, Singleton
 * @author Oliver Grande
 * Created: 26.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET, Applicability.SINGLETON })
public @interface UpdateRestrictions {
  /**
   * Entities can be updated
   */
  boolean updatable() default true;

  /**
   * Entities can be upserted
   */
  boolean upsertable() default false;

//  /**
//   * Delta handling not supported yet
//   * Entities can be inserted, updated, and deleted via a PATCH request with a delta payload
//   */
//  boolean deltaUpdateSupported() default false;

  /**
   * Supported HTTP Methods (PUT or PATCH) for updating an entity. If null, PATCH SHOULD be supported and PUT MAY be
   * supported.<br>
   */
  UpdateMethod updateMethod() default UpdateMethod.NOT_SPECIFIED;

//<Property Name="FilterSegmentSupported" Type="Edm.Boolean" Nullable="false" DefaultValue="true">
//  <Annotation Term="Core.Description" String="Members of collections can be updated via a PATCH request with a `/$filter(...)/$each` segment" />
//</Property>
//<Property Name="TypecastSegmentSupported" Type="Edm.Boolean" Nullable="false" DefaultValue="true">
//  <Annotation Term="Core.Description" String="Members of collections can be updated via a PATCH request with a type-cast segment and a `/$each` segment" />
//</Property>
  /**
   * These structural properties cannot be specified on update
   */
  String[] nonUpdatableProperties() default {};

  /**
   * These navigation properties do not allow rebinding
   */
  String[] nonUpdatableNavigationProperties() default {};

  /**
   * These structural properties must be specified on update
   */
  String[] requiredProperties() default {};

  /**
   * The maximum number of navigation properties that can be traversed when addressing the collection or entity to
   * update. A value of -1 indicates there is no restriction.
   */
  int maxLevels() default -1;

//<Property Name="Permissions" Type="Collection(Capabilities.PermissionType)" Nullable="true">
//  <Annotation Term="Core.Description" String="Required permissions. One of the specified sets of scopes is required to perform the update." />
//</Property>
//<Property Name="QueryOptions" Type="Capabilities.ModificationQueryOptionsType" Nullable="true">
//  <Annotation Term="Core.Description" String="Support for query options with update requests" />
//</Property>
//<Property Name="CustomHeaders" Type="Collection(Capabilities.CustomParameter)" Nullable="false">
//  <Annotation Term="Core.Description" String="Supported or required custom headers" />
//</Property>
//<Property Name="CustomQueryOptions" Type="Collection(Capabilities.CustomParameter)" Nullable="false">
//  <Annotation Term="Core.Description" String="Supported or required custom query options" />
//</Property>
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
