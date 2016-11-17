package com.sap.olingo.jpa.processor.core.processor;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;

public interface JPARequestProcessor {

  public void retrieveData(ODataRequest request, ODataResponse response, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException, ODataException;
}
