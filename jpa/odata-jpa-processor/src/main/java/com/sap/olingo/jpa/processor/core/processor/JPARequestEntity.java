package com.sap.olingo.jpa.processor.core.processor;

import java.util.Map;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * Representing an entity that should be created or updated by a POST, PUT or PATCH request
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
   * List of attributes with pojo attributes name and converted into JAVA types. In case the entity contains embedded
   * attributes these are given as maps themselves.<p>
   * Deep insert list<JPARequestEntity>, list<JPARequestLink>
   * @return
   */
  public Map<String, Object> getData();

  /**
   * Contains the key attributes of the entity to be update
   * @return
   */
  public Map<String, Object> getKeys();

  /**
   * Helper
   * @param st
   * @param jpaAttributes
   * @param instanze
   * @throws ODataJPAProcessorException
   */
  public void setAttributes(final JPAStructuredType st, final Map<String, Object> jpaAttributes, final Object instanze)
      throws ODataJPAProcessorException;
}
