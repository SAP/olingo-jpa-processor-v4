package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationAttribute extends JPAAttribute {

  public JPAStructuredType getTargetEntity() throws ODataJPAModelException;

  public JPAAssociationAttribute getPartner() throws ODataJPAModelException;

  public JPAAssociationPath getPath() throws ODataJPAModelException;

}
