package com.sap.olingo.jpa.processor.core.converter;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPAExpandResult {

  List<Tuple> getResult(final String key);

  Map<JPAAssociationPath, JPAExpandResult> getChildren();

  boolean hasCount();

  Integer getCount();

  JPAEntityType getEntityType();

}