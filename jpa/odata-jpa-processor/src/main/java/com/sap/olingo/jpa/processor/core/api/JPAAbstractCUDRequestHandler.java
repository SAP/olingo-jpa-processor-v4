package com.sap.olingo.jpa.processor.core.api;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;

public abstract class JPAAbstractCUDRequestHandler implements JPACUDRequestHandler {

  @Override
  public void deleteEntity(final JPAEntityType et, final Map<String, Object> keyPredicates, final EntityManager em)
      throws ODataJPAProcessException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public Object createEntity(final JPAEntityType et, final Map<String, Object> jpaAttributes, final EntityManager em)
      throws ODataJPAProcessException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);

  }

  @Override
  public JPAUpdateResult updateEntity(final JPAEntityType et, final Map<String, Object> jpaAttributes,
      final Map<String, Object> keys, final EntityManager em, final HttpMethod method) throws ODataJPAProcessException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }
}
