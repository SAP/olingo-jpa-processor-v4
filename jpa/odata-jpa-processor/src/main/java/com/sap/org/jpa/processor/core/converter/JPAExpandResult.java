package com.sap.org.jpa.processor.core.converter;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;

public interface JPAExpandResult {

  List<Tuple> getResult(final String key);

  Map<JPAAssociationPath, JPAExpandResult> getChildren();

  boolean hasCount();

  Integer getCount();

  JPAEntityType getEntityType();

}