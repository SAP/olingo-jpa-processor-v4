package com.sap.olingo.jpa.processor.core.query;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public abstract class JPANavigationQuery extends JPAAbstractQuery {

  public JPANavigationQuery(OData odata, JPAServiceDocument sd, EdmEntityType edmEntityType, EntityManager em)
      throws ODataApplicationException {
    super(odata, sd, edmEntityType, em);
  }

  public abstract <T extends Object> Subquery<T> getSubQueryExists(final Subquery<?> childQuery)
      throws ODataApplicationException;
}
