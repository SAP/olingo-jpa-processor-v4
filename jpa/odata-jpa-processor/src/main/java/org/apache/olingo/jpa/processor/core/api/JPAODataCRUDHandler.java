package org.apache.olingo.jpa.processor.core.api;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;

public class JPAODataCRUDHandler extends JPAODataGetHandler {

  public JPAODataCRUDHandler(String pUnit) throws ODataException {
    super(pUnit);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  public JPAODataCRUDHandler(final String pUnit, final DataSource ds) throws ODataException {
    super(pUnit, ds);
    getJPAODataContext().setCUDRequestHandler(new JPADefaultCUDRequestHandler());
  }

  @Override
  public JPAODataCRUDContext getJPAODataContext() {
    return (JPAODataCRUDContext) super.getJPAODataContext();
  }

  class JPADefaultCUDRequestHandler extends JPAAbstractCUDRequestHandler {

  }
}
