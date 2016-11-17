package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

public interface ODataJPAMessageBufferRead {

  String getText(Object execption, String ID);

  String getText(Object execption, String ID, String... parameters);

}