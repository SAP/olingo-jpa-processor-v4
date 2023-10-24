package com.sap.olingo.jpa.processor.core.processor;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;

public interface JPARequestProcessor {

  public <K extends Comparable<K>> void retrieveData(ODataRequest request, ODataResponse response,
      ContentType responseFormat) throws ODataException;
}
