package com.sap.olingo.jpa.processor.core.query;

import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPAExpandItem extends UriInfoResource {

  JPAEntityType getEntityType();

}