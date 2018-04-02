package com.sap.olingo.jpa.processor.core.query;

import java.util.Map;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;

public interface JPAConvertableResult {

  Map<String, EntityCollection> asEntityCollection(final JPATupleChildConverter converter) throws ODataApplicationException;

  void putChildren(final Map<JPAAssociationPath, JPAExpandResult> childResults) throws ODataApplicationException;

}
