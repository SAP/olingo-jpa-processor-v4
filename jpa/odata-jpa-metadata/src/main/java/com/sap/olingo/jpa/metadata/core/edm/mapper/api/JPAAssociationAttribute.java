package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

//TODO remove extension
public interface JPAAssociationAttribute extends JPAAttribute {

  public JPAStructuredType getTargetEntity() throws ODataJPAModelException;

  public JPAAssociationAttribute getPartner();

  public JPAAssociationPath getPath() throws ODataJPAModelException;

}
