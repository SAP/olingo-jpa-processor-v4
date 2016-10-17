package org.apache.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;

public interface JPACUDRequestHandler {
  public void deleteEntity(final JPAEntityType et, final Map<String, Object> keyPredicates, EntityManager em)
      throws ODataJPAProcessException;

  /**
   * 
   * @param et Entity type that shall be created
   * @param jpaAttributes List of attributes with pojo attributes name and converted into JAVA types
   * @param em Entity manager
   * @param headers List of all headers
   * @return
   * @throws ODataJPAProcessException
   */
  public Entity createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em,
      Map<String, List<String>> headers) throws ODataJPAProcessException;
}
