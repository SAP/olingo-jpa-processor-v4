package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;

public final class JPACollectionItemInfo extends JPAInlineItemInfo {

  JPACollectionItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationPropertyInfo> parentHops) {

    super(uriInfo, expandAssociation, parentHops);

    for (final JPANavigationPropertyInfo predecessor : parentHops)
      this.hops.add(new JPANavigationPropertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    this.hops.add(new JPANavigationPropertyInfo(sd, expandAssociation, uriInfo, uriInfo.getEntityType()));
  }

  @Override
  public String toString() {
    return "JPACollectionItemInfo [expandAssociation=" + expandAssociation + "]";
  }
}
