package com.sap.olingo.jpa.processor.core.query;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAQuery {
  JPAConvertableResult execute() throws ODataApplicationException;
}
