package com.sap.olingo.jpa.processor.core.api;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.olingo.server.api.ODataRequest;

public record JPAODataPathInformation(String baseUri, String oDataPath, @Nullable String queryPath,
    @Nullable String fragments) implements Serializable {

  public JPAODataPathInformation(final ODataRequest request) {
    this(request.getRawBaseUri(), request.getRawODataPath(), request.getRawQueryPath(), null);
  }

}
