package com.sap.olingo.jpa.processor.core.query;

import java.util.Optional;

import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;

public interface JPAExpandItem extends UriInfoResource {

  JPAEntityType getEntityType();

  Optional<JPAODataSkipTokenProvider> getSkipTokenProvider();

}