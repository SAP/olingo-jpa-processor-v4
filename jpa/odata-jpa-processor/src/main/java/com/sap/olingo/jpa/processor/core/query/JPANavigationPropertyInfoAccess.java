package com.sap.olingo.jpa.processor.core.query;

import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;

public interface JPANavigationPropertyInfoAccess {

  JPAAssociationPath getAssociationPath();

  UriResourcePartTyped getUriResource();

}