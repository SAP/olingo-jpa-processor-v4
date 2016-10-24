package org.apache.olingo.jpa.processor.core.modify;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;

public interface JPACUDRequestHandler {
  /**
   * 
   * @param et
   * @param keyPredicates
   * @param em
   * @throws ODataJPAProcessException
   */
  public void deleteEntity(final JPAEntityType et, final Map<String, Object> keyPredicates, EntityManager em)
      throws ODataJPAProcessException;

  /**
   * 
   * @param et Entity type that shall be created
   * @param jpaAttributes List of attributes with pojo attributes name and converted into JAVA types
   * @param em Entity manager
   * @return The newly created instance
   * @throws ODataJPAProcessException
   */
  public Object createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em)
      throws ODataJPAProcessException;

  /**
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300"
   * >OData Version 4.0 Part 1 - 11.4.3 Update an Entity</a>
   * @param et Entity type that shall be created
   * @param jpaAttributes List of attributes with pojo attributes name and converted into JAVA types
   * @param keys List of keys defined in the URI with pojo attributes name and converted into JAVA types
   * @param em Entity manager
   * @param method Method (PUT/PATCH) used for update
   * @return
   * @throws ODataJPAProcessException
   */
  public Object updateEntity(JPAEntityType et, Map<String, Object> jpaAttributes, Map<String, Object> keys,
      EntityManager em, HttpMethod method)
      throws ODataJPAProcessException;
}
