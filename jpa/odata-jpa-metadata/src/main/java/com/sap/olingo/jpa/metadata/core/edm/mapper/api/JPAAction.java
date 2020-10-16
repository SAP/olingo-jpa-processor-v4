package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Parameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAction extends JPAOperation, JPAJavaOperation {

  JPAParameter getParameter(Parameter declaredParameter) throws ODataJPAModelException;
}
