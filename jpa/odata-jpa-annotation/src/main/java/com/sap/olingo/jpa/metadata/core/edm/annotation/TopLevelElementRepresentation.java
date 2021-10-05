package com.sap.olingo.jpa.metadata.core.edm.annotation;

/**
 * @author Oliver Grande
 * Created: 01.05.2021
 *
 */
public enum TopLevelElementRepresentation {
  /**
   * Allows to state that a jpa entity is exposed as odata entity type only.
   */
  AS_ENTITY_TYPE,
  /**
   * Allows to state that a jpa entity is exposed not only as odata entity type only, but also as entity set.
   */
  AS_ENTITY_SET,
  /**
   * Allows to mark a jpa entity as an additional entity set of another entity type.
   * This is only allowed for leafs in an inheritance hierarchy. The jpa entity must not have own columns<p>
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752596"
   * >OData Version 4.0 Part 3 - 13 Entity Container Example 30</a>
   */
  AS_ENTITY_SET_ONLY,
  /**
   * Allows to state that a jpa entity is exposed not only as odata entity type only, but also as singleton.
   */
  AS_SINGLETON,
  /**
   * Allows to mark a jpa entity as an additional singleton of another entity type.
   * This is only allowed for leafs in an inheritance hierarchy. The jpa entity must not have own columns<p>
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752596"
   * >OData Version 4.0 Part 3 - 13 Entity Container Example 30</a>
   */
  AS_SINGLETON_ONLY;
}
