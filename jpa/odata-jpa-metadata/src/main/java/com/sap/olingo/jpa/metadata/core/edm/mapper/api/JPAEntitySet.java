package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEntitySet extends JPATopLevelEntity {

  JPAEntityType getODataEntityType() throws ODataJPAModelException;

  JPAEntityType getEntityType();

}
