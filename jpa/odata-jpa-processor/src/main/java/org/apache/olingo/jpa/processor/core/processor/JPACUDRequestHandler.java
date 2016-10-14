package org.apache.olingo.jpa.processor.core.processor;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;

public interface JPACUDRequestHandler {
  public void deleteEntity(final JPAEntityType et, final Map<String, Object> keyPredicates, EntityManager em)
      throws ODataJPAProcessException;
}
