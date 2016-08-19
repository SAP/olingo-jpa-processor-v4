package org.apache.olingo.jpa.processor.core.database;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

public interface JPAODataDatabaseSearch {
  /**
   * Search at OData:<p>
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793700">
   * OData Version 4.0 Part 1 - 11.2.5.6 System Query Option $search</a><p>
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/os/part2-url-conventions/odata-v4.0-os-part2-url-conventions.html#_Toc372793865">
   * OData Version 4.0 Part 2 - 5.1.7 System Query Option $search</a>
   * @param cb
   * @param cq
   * @param root
   * @param entityType
   * @param searchOption
   * @return
   * @throws ODataApplicationException
   */
  Expression<Boolean> createSearchWhereClause(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<?> root,
      JPAEntityType entityType, SearchOption searchOption) throws ODataApplicationException;
}
