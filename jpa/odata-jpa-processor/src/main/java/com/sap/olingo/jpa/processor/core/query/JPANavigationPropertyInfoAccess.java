package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;

public interface JPANavigationPropertyInfoAccess {

  JPAAssociationPath getAssociationPath();

  UriResourcePartTyped getUriResource();

  List<UriParameter> getKeyPredicates();

}