package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAFunctionParamaterFacet {

  Integer getMaxLength();

  Integer getPrecision();

  Integer getScale();

  Class<?> getType();

  FullQualifiedName getTypeFQN() throws ODataJPAModelException;
}