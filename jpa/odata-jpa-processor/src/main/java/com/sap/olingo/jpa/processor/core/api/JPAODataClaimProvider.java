package com.sap.olingo.jpa.processor.core.api;

import java.util.List;

/**
 * Container that provides claims
 * 
 * @author Oliver Grande
 * Created: 30.06.2019
 *
 */
public interface JPAODataClaimProvider {

  List<JPAClaimsPair<?>> get(final String attributeName); // NOSONAR

}