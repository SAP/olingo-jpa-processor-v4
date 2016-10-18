package org.apache.olingo.jpa.processor.core.api;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.processor.JPACUDRequestHandler;

public abstract class JPAAbstractCUDRequestHandler implements JPACUDRequestHandler {

  @Override
  public void deleteEntity(JPAEntityType et, Map<String, Object> keyPredicates, EntityManager em)
      throws ODataJPAProcessException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public Object createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em)
      throws ODataJPAProcessException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);

  }
}
