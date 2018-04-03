package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPACollectionItemInfo extends JPAInlineItemInfo {

  JPACollectionItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationProptertyInfo> hops) {

    super(uriInfo, expandAssociation);

    for (JPANavigationProptertyInfo predecessor : hops)
      this.hops.add(new JPANavigationProptertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    this.hops.add(new JPANavigationProptertyInfo(sd, expandAssociation, uriInfo, uriInfo.getEntityType()));
  }
}
