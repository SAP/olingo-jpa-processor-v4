package com.sap.olingo.jpa.processor.core.filter;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

/**
 * Main purpose of this interface is to increase testability of JPAEnumerationBasedOperator
 * @author Oliver Grande
 *
 */
public interface JPAEnumerationBasedOperator extends JPAPrimitiveTypeOperator {

  Number getValue() throws ODataJPAFilterException;

}