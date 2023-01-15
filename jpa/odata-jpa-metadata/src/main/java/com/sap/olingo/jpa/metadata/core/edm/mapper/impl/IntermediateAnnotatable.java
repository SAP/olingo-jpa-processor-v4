package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

interface IntermediateAnnotatable {

  /**
   * Converts a path given as a string of internal (Java) attribute names into a JPAPath.
   * @param internalPath
   * @return
   * @throws ODataJPAModelException
   */
  JPAPath convertStringToPath(final String internalPath) throws ODataJPAModelException;

  JPAAssociationPath convertStringToNavigationPath(final String internalPath) throws ODataJPAModelException;

}
