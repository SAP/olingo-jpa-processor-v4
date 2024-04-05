package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;

interface JPAFilterComplierAccess {

  /**
   * @return Query a filter belongs to.
   */
  JPAAbstractQuery getParent();

  List<UriResource> getUriResourceParts();

  JPAServiceDocument getSd();

  OData getOData();

  EntityManager getEntityManager();

  JPAEntityType getJpaEntityType();

  JPAOperationConverter getConverter();

  /**
   * Root of the query the filter belongs to.
   * @param <S>
   * @param <T>
   * @return
   */
  <S, T> From<S, T> getRoot();

  JPAServiceDebugger getDebugger();

  JPAAssociationPath getAssociation();

  Optional<JPAODataClaimProvider> getClaimsProvider();

  List<String> getGroups();

  Optional<JPAFilterRestrictionsWatchDog> getWatchDog();

}
