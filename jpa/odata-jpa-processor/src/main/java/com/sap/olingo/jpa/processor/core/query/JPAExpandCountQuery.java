package com.sap.olingo.jpa.processor.core.query;

import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;

/**
 *
 * @author Oliver Grande
 * @since 2.2.0
 * 2024-08-25
 *
 */
public interface JPAExpandCountQuery {

  Map<String, Long> count() throws ODataApplicationException;

}
