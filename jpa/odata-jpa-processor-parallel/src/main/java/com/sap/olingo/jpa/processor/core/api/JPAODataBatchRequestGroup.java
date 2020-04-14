package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

interface JPAODataBatchRequestGroup {
  /**
   * 
   * @return
   * @throws ODataJPABatchRuntimeException
   */
  List<ODataResponsePart> execute();
}