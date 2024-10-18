package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;

public final class JPAExpandItemInfo extends JPAInlineItemInfo {
  private static final Log LOGGER = LogFactory.getLog(JPAExpandItemInfo.class);

  JPAExpandItemInfo(final JPAServiceDocument sd, final JPAExpandItem uriInfo,
      final JPAAssociationPath expandAssociation, final List<JPANavigationPropertyInfo> hops)
      throws ODataApplicationException {

    super(uriInfo, expandAssociation, hops);

    for (final JPANavigationPropertyInfo predecessor : hops)
      this.hops.add(new JPANavigationPropertyInfo(predecessor));
    this.hops.get(this.hops.size() - 1).setAssociationPath(expandAssociation);
    if (!uriInfo.getUriResourceParts().isEmpty())
      this.hops.addAll(Utility.determineNavigationPath(sd, uriInfo.getUriResourceParts(), uriInfo));
    else
      this.hops.add(new JPANavigationPropertyInfo(sd, null, uriInfo, uriInfo.getEntityType()));
  }

  public JPAExpandItemInfo(final JPAExpandItemInfo expandItem, final Optional<JPAODataExpandPage> page) {

    super(setPage(expandItem.uriInfo, expandItem.expandAssociation, page), expandItem.expandAssociation,
        expandItem.parentHops);
    this.hops.addAll(expandItem.hops);
  }

  private static JPAExpandItem setPage(final JPAExpandItem uriInfo, final JPAAssociationPath expandAssociation,
      final Optional<JPAODataExpandPage> page) {
    if (page.isPresent() && uriInfo instanceof final JPAExpandItemPageable pageable) {
      LOGGER.trace("Server driven paging found on " + expandAssociation.getAlias());
      pageable.setPage(page.get());
    }
    return uriInfo;
  }
}
