package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.commons.api.ex.ODataException;

public final class JPAODataCRUDHandler extends JPAODataGetHandler {

  public JPAODataCRUDHandler(String pUnit) throws ODataException {
    super(pUnit);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  public JPAODataCRUDHandler(final String pUnit, final DataSource ds) throws ODataException {
    super(pUnit, ds);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  public JPAODataCRUDHandler(final String pUnit, final DataSource ds, final EntityManagerFactory emf, JPAEdmProvider edmProvider) throws ODataException {
    super(pUnit, ds, emf, edmProvider);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  @Override
  public JPAODataCRUDContext getJPAODataContext() {
    return (JPAODataCRUDContext) super.getJPAODataContext();
  }

  class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
