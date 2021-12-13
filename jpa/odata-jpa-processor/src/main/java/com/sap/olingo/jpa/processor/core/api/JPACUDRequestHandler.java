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
   * @param requestEntity
   * @param em
   * @return The newly created instance or map of created attributes including default and added values
   * following the same rules as jpaAttributes
   * @throws ODataJPAProcessException
   */
  public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessException;

  /**
   * Hook to handle all request that change an existing entity.
   * This includes update and upsert on entities, updates on properties and values, updates on relations as well as
   * deletions on values and properties. <br>
   * <b>Note:</b> Deviating from the OData standard changes on collection properties will be provided as PATCH, as it is
   * from an entity point of view is the partial change. An
   * implementation needs to take care that all elements of the collection are exchanged!
   * </p>
   * @see
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300"
   * >OData Version 4.0 Part 1 - 11.4.3 Update an Entity</a><br>
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752301"
   * >OData Version 4.0 Part 1 - 11.4.4 Upsert an Entity</a><br>
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752303"
   * >OData Version 4.0 Part 1 - 11.4.6 Modifying Relationships between Entities</a><br>
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306"
   * >OData Version 4.0 Part 1 - 11.4.9 Managing Values and Properties Directly</a><br>
   *
   * @param requestEntity See {@link com.sap.olingo.jpa.processor.core.processor.JPARequestEntity JPARequestEntity}
   * @param em Instance of an entity manager with an open transaction.
   * @param httpMethod The original http method: PATCH, PUT, DELETE
   * @return The response describes the performed changes (Created or Updated) as well as the result of the operation.
   * It must not be null. Even if nothing was changed => update is idempotent
   * @throws ODataJPAProcessException
   */
  public JPAUpdateResult updateEntity(final JPARequestEntity requestEntity, final EntityManager em,
      final HttpMethod httpMethod) throws ODataJPAProcessException;

  /**
   * Hook that is called after all changes of one transaction have been processed. The method shall enable a check of
   * all modification within the new context. This can be imported if multiple entities are changes with the same
   * request (batch request or deep-insert) and consistency constrains exist between them.<p>
   * In case changes are made to the entities, these changes are not part of the response in case of batch requests.
   * @throws ODataJPAProcessException
   */
  public void validateChanges(final EntityManager em) throws ODataJPAProcessException;
}
