package com.sap.olingo.jpa.processor.cb.impl;

import jakarta.persistence.criteria.Path;

import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

/**
 *
 * @author D023143
 * @since 2.0.1
 * @created 12.11.2023
 */
interface CompoundPath extends SqlConvertible {

  boolean isEmpty();

  Path<?> getFirst() throws IllegalStateException;

}
