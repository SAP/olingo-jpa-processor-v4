package com.sap.olingo.jpa.processor.core.filter;

/**
 * Represents an operator that provides a path value for related entities.
 */
public interface JPAPathOperator extends JPAOperator {

    /**
     * Retrieves the path value for the operator.
     *
     * @return the path value as an Object.
     */
    Object getPathValue();
}