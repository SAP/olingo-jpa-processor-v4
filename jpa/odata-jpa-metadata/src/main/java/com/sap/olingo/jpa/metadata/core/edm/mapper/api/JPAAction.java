package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Parameter;

public interface JPAAction extends JPAOperation, JPAJavaOperation {

  JPAParameter getParameter(Parameter declairedParameter);

}
