package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPAExpandItemInfo extends JPAInlineItemInfo {

  JPAExpandItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationPropertyInfo> hops)
      throws ODataApplicationException {

    super(uriInfo, expandAssociation, hops);

    for (JPANavigationPropertyInfo predecessor : hops)
      this.hops.add(new JPANavigationPropertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    if (!uriInfo.getUriResourceParts().isEmpty())
      this.hops.addAll(Utility.determineNavigationPath(sd, uriInfo.getUriResourceParts(), uriInfo));
    else
      this.hops.add(new JPANavigationPropertyInfo(sd, null, uriInfo, uriInfo.getEntityType()));
  }
}
