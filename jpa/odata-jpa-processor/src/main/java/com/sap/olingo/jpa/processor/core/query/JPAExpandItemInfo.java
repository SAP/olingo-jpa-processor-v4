package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPAExpandItemInfo extends JPAInlineItemInfo {

  JPAExpandItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationProptertyInfo> hops)
      throws ODataApplicationException {

    super(uriInfo, expandAssociation);

    for (JPANavigationProptertyInfo predecessor : hops)
      this.hops.add(new JPANavigationProptertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    if (!uriInfo.getUriResourceParts().isEmpty())
      this.hops.addAll(Util.determineNavigationPath(sd, uriInfo.getUriResourceParts(), uriInfo));
    else
      this.hops.add(new JPANavigationProptertyInfo(sd, null, uriInfo, uriInfo.getEntityType()));
  }
}
