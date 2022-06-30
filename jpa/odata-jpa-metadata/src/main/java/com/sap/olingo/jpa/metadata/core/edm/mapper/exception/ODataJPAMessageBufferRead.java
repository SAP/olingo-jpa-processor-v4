package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

public interface ODataJPAMessageBufferRead {

  String getText(Object execution, String iD);

  String getText(Object execution, String iD, String... parameters);

}