package com.sap.olingo.jpa.processor.core.query;

/**
 * A query to retrieve the expand entities.<p> According to
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
 * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> the following query options are allowed:
 * <ul>
 * <li>expandCountOption = <b>filter</b>/ search<p>
 * <li>expandRefOption = expandCountOption/ <b>orderby</b> / <b>skip</b> / <b>top</b> / inlinecount
 * <li>expandOption = expandRefOption/ <b>select</b>/ <b>expand</b> / <b>levels</b> <p>
 * </ul>
 * As of now only the bold once are supported
 * <p>
 * @author Oliver Grande
 * @since 0.3.6
 */
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;

public class JPAExpandSubQuery extends JPAAbstractJoinQuery {

  public JPAExpandSubQuery(final OData odata, final JPAODataCRUDContextAccess sessionContext,
      final JPAInlineItemInfo item, final Map<String, List<String>> requestHeaders,
      final JPAODataRequestContextAccess requestContext, final SortedSet<? extends Comparable<?>> rootKeys)
      throws ODataException {

    super(odata, sessionContext, item.getEntityType(), item.getUriInfo(), requestContext, requestHeaders,
        item.getHops());
  }

  public <K extends Comparable<K>> JPAExpandSubQuery(final OData odata, final JPAODataCRUDContextAccess context,
      final JPAAssociationPath association, final JPAEntityType entityType,
      final Map<String, List<String>> requestHeaders, final JPAODataRequestContextAccess requestContext,
      final SortedSet<K> rootKeys)
      throws ODataException {

    super(odata, context, entityType, requestContext, requestHeaders, Collections.emptyList());

  }

  @Override
  public JPAConvertableResult execute() throws ODataApplicationException {
    return null;
  }
}
