package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;

/**
 * Representing an entity that should be created, updated or deleted by a POST, PUT, PATCH or DELETE request
 * @author Oliver Grande
 *
 */
public interface JPARequestEntity {
  /**
   * Provides an instance of the entity metadata
   * @return
   */
  public JPAEntityType getEntityType();

  /**
   * List of attributes with pojo attributes name converted into JAVA types. In case the entity contains embedded
   * attributes these are given as maps themselves.<p>
   * @return
   */
  public Map<String, Object> getData();

  /**
   * Contains the key attributes of the entity to be update or deleted. Returns an empty Map in case of create.
   * @return
   */
  public Map<String, Object> getKeys();

  /**
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299">
   * 11.4.2.1 Link to Related Entities When Creating an Entity</a>
   * @return
   */
  public Map<JPAAssociationPath, List<JPARequestLink>> getRelationLinks();

  /**
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299">
   * 11.4.2.2 Create Related Entities When Creating an Entity</a>
   * @return
   */
  public Map<JPAAssociationPath, List<JPARequestEntity>> getRelatedEntities();

  /**
   * Returns all OData request header
   * @return an unmodifiable Map of header names/values
   */
  public Map<String, List<String>> getAllHeader();

  /**
   * Returns an instance utility service
   * @return
   */
  public JPAModifyUtil getModifyUtil();

  /**
   * Provides the given claims of a user
   * @return
   */
  public Optional<JPAODataClaimsProvider> getClaims();

  /**
   * For the creation of a dependent entity an instance of the requested entity (root entity) is provided. <br> The
   * instance must not be merged
   * @return
   */
  public Optional<Object> getBeforeImage();

}
