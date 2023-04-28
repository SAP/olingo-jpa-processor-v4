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
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L787"><i>DeleteRestrictions</i></a>:
 * <br>
 * Restrictions on delete operations.
 * <p>
 *
 * AppliesTo: EntitySet Singleton EntityType
 * @author Oliver Grande
 * Created: 29.04.2021
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Vocabulary(alias = "Capabilities", appliesTo = { Applicability.ENTITY_SET, Applicability.SINGLETON })
public @interface DeleteRestrictions {
  /**
   * Entities can be deleted
   */
  boolean deletable() default true;

  /**
   * These navigation properties do not allow DeleteLink requests
   */
  String[] nonDeletableNavigationProperties() default {};

  /**
   * The maximum number of navigation properties that can be traversed when addressing the collection to delete from or
   * the entity to delete. A value of -1 indicates there is no restriction.
   */
  int maxLevels() default -1;

//  <Property Name="FilterSegmentSupported" Type="Edm.Boolean" Nullable="false" DefaultValue="true">
//    <Annotation Term="Core.Description" String="Members of collections can be updated via a PATCH request with a `/$filter(...)/$each` segment" />
//  </Property>
//  <Property Name="TypecastSegmentSupported" Type="Edm.Boolean" Nullable="false" DefaultValue="true">
//    <Annotation Term="Core.Description" String="Members of collections can be updated via a PATCH request with a type-cast segment and a `/$each` segment" />
//  </Property>
//  <Property Name="Permissions" Type="Collection(Capabilities.PermissionType)" Nullable="true">
//    <Annotation Term="Core.Description" String="Required permissions. One of the specified sets of scopes is required to perform the delete." />
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
