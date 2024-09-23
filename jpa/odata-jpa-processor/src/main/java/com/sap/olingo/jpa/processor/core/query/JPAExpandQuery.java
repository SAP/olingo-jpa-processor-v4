package com.sap.olingo.jpa.processor.core.query;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAExpandQuery {
  JPAExpandQueryResult execute() throws ODataApplicationException;
}
