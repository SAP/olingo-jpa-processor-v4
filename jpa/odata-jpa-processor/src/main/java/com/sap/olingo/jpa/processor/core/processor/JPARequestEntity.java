package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPATopLevelEntity;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;

/**
 * Representing an entity that should be created, updated or deleted by a POST, PUT, PATCH or DELETE request
 * @author Oliver Grande
 *
 */
public interface JPARequestEntity {
  /**
   * Returns all OData request header
   * @return an unmodifiable Map of header names/values
   */
  public Map<String, List<String>> getAllHeader();

  /**
   * For the creation of a dependent entity an instance of the requested entity (root entity) is provided. <br>
   * The
   * instance must not be merged
   * @return
   */
  public Optional<Object> getBeforeImage();

  /**
   * Provides the given claims of a user
   * @return
   */
  public Optional<JPAODataClaimProvider> getClaims();

  /**
   * List of attributes with pojo attributes name converted into JAVA types. In case the entity contains embedded
   * attributes these are given as maps themselves.
   * <p>
   * @return
   */
  public Map<String, Object> getData();

  /**
   * Provides an instance of the entity metadata
   * @return
   */
  public JPAEntityType getEntityType();

  /**
   * Provides an instance to access the Singleton or Entity Set metadata
   * @return
   */
  public Optional<JPATopLevelEntity> getTopLevelEntity();

  /**
   * Returns a list of given filed groups
   * @return
   */
  public List<String> getGroups();

  /**
   * Contains the key attributes of the entity to be update or deleted. Returns an empty Map in case of create.
   * @return
   */
  public Map<String, Object> getKeys();

  /**
   * Returns an instance utility service
   * @return
   */
  public JPAModifyUtil getModifyUtil();

  /**
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299">
   * 11.4.2.2 Create Related Entities When Creating an Entity</a>
   * @return
   */
  public Map<JPAAssociationPath, List<JPARequestEntity>> getRelatedEntities();

  /**
   * <a href=
   * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299">
   * 11.4.2.1 Link to Related Entities When Creating an Entity</a>
   * @return
   */
  public Map<JPAAssociationPath, List<JPARequestLink>> getRelationLinks();

}
