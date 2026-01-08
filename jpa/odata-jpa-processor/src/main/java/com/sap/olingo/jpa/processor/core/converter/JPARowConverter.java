package com.sap.olingo.jpa.processor.core.converter;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

public interface JPARowConverter {

  Entity convertRow(JPAEntityType rowEntity, Tuple row, Collection<JPAPath> requestedSelection,
      List<JPAODataPageExpandInfo> expandInfo, JPAExpandResult jpaResult) throws ODataApplicationException;

}