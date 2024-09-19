package com.sap.olingo.jpa.processor.core.processor;

import javax.annotation.Nonnull;

import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

interface ODATARequestContext {

  /**
   *
   * @param uriInfo
   * @throws ODataJPAIllegalAccessException In case UriInfo already exists e.g. because a page was provided
   */
  void setUriInfo(@Nonnull final UriInfo uriInfo) throws ODataJPAIllegalAccessException;

  void setJPASerializer(@Nonnull final JPASerializer serializer);
}
