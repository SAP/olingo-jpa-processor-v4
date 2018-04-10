package com.sap.olingo.jpa.processor.core.api.example;

import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;

public class JPAExamplePagingProvider implements JPAODataPagingProvider {

  @Override
  public JPAODataPage getNextPage(final String skiptoken) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPAODataPage getFristPage(final UriInfo uriInfo) {
    // TODO Auto-generated method stub
    return null;
  }

}
