package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;

public class JPAJoinCountQuery extends JPAAbstractRootJoinQuery implements JPACountQuery {

  public JPAJoinCountQuery(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, requestContext);
  }

  /**
   * Fulfill $count requests. For details see
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752288"
   * >OData Version 4.0 Part 1 - 11.2.5.5 System Query Option $count</a>
   * @return
   * @throws ODataApplicationException
   */
  @Override
  public Long countResults() throws ODataApplicationException {
    /*
     * URL example:
     * .../Organizations?$count=true
     * .../Organizations/$count
     * .../Organizations('3')/Roles/$count
     */
    try (var measurement = debugger.newMeasurement(this, "countResults")) {

      new JPACountWatchDog(entitySet.map(JPAAnnotatable.class::cast)).watch(this.uriResource);
      createFromClause(Collections.emptyList(), Collections.emptyList(), cq, lastInfo);
      final var whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);
      cq.multiselect(cb.count(target));
      final var result = em.createQuery(cq).getSingleResult();
      return ((Number) result.get(0)).longValue();
    } catch (final JPANoSelectionException e) {
      return 0L;
    }
  }
}
