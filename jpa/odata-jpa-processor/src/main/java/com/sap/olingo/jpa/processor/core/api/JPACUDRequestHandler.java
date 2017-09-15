package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpMethod;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.processor.JPARequestEntity;

public interface JPACUDRequestHandler {
  /**
   * 
   * @param requestEntity
   * @param em
   * @throws ODataJPAProcessException
   */
  public void deleteEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException;

  /**
   * Hook to create an entity. Transaction handling is done outside to guarantee transactional behavior of change
   * sets in batch requests. This method has to return the newly create entity even so validateChanges is implemented.
   * 
   * @param et Metadata about the entity type that shall be created
   * @param jpaAttributes List of attributes with pojo attributes name and converted into JAVA types
   * @param em Instance of an entity manager.
   * @return The newly created instance or map of created attributes including default and added values
   * following the same rules as jpaAttributes
   * @throws ODataJPAProcessException
   */

  /**
   * 
   * @param requestEntity
   * @param em
   * @return The newly created instance or map of created attributes including default and added values
   * following the same rules as jpaAttributes
   * @throws ODataJPAProcessException
   */
  public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException;

  /**
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300"
   * >OData Version 4.0 Part 1 - 11.4.3 Update an Entity</a>
   * @param requestEntity
   * @param em
   * @param httpMethod
   * @return The response describes the performed changes (Created or Updated) as well as the result of the operation.
   * It must not be null. Even if nothing was changed => update is idempotent
   * @throws ODataJPAProcessException
   */
  public JPAUpdateResult updateEntity(final JPARequestEntity requestEntity, final EntityManager em,
      final HttpMethod httpMethod) throws ODataJPAProcessException;

  /**
   * Hook that is called if all changes of one transaction have been processed. The method shall enable a check all
   * modification within the new context. This can be imported if multiple entities are changes with the same request
   * (batch request or deep-insert) and consistency constrains exist between them.
   * @throws ODataJPAProcessException
   */
  public void validateChanges(final EntityManager em) throws ODataJPAProcessException;
}
