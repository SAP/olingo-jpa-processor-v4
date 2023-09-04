package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPATopLevelEntity extends JPAElement, JPAAnnotatable {
  public Optional<JPAQueryExtension<EdmQueryExtensionProvider>> getQueryExtension() throws ODataJPAModelException;
}
