package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Root;

import org.apache.olingo.server.api.ODataApplicationException;

/**
 * Handle OData Functions that are implemented e.g. as user defined functions data base functions. This will be mapped
 * to JPA criteria builder function().
 * 
 * @author Oliver Grande
 *
 */
public class JPAFunctionOperator implements JPAOperator {

  public JPAFunctionOperator(Root<?> root, JPAOperationConverter converter) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Object get() throws ODataApplicationException {
    // TODO Auto-generated method stub
    return null;
  }

}
