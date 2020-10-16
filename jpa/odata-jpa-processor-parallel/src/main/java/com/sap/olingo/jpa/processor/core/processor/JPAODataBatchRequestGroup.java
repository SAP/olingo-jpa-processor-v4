package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;

import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

public interface JPAODataBatchRequestGroup {
  /**
   * 
   * @return
   * @throws ODataJPABatchRuntimeException
   */
  List<ODataResponsePart> execute();
}