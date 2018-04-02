package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPACollectionAttribute extends JPAAttribute {

  JPAAssociationPath asAssociation() throws ODataJPAModelException;

}
