package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;
import com.sap.olingo.jpa.processor.core.query.JPAExpandCountQuery;

/**
 * Supporting
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752288">
 * Server Drive Paging (Part1 11.2.5.7)</a>
 * @author Oliver Grande
 *
 */
public interface JPAODataPagingProvider {
  /**
   * Returns the page related to a given skiptoken.
   * If the skiptoken is not known the method must return null.
   * @param skipToken
   * @return Next page. If the page is null, an exception
   * @deprecated implement
   * {@link #getNextPage(String, OData, ServiceMetadata, JPARequestParameterMap, EntityManager)}
   * instead
   */
  @Deprecated(since = "2.1.0", forRemoval = true)
  default JPAODataPage getNextPage(@Nonnull final String skipToken) {
    return getNextPage(skipToken, null, null, null, null).orElse(null);
  }

  /**
   * Returns the page related to a given skiptoken.
   * If the skiptoken is not known the method must return an empty optional.
   * @param skipToken
   * @param odata
   * @param serviceMetadata
   * @param requestParameter
   * @return
   */
  default Optional<JPAODataPage> getNextPage(@Nonnull final String skipToken, final OData odata,
      final ServiceMetadata serviceMetadata, final JPARequestParameterMap requestParameter, final EntityManager em) {
    return Optional.ofNullable(getNextPage(skipToken)); // NOSONAR
  }

  /**
   * Based on the query the provider decides if a paging is required and return the first page.
   * @param uriInfo
   * @param preferredPageSize Value from odata.maxpagesize preference header
   * @param countQuery A query that can be used to determine the maximum number of results that can be expected. Only if
   * the number of expected results is bigger then the page size a next link
   * @param em
   * @return
   * @throws ODataApplicationException
   * @deprecated implement
   * {@link #getFirstPage(JPARequestParameterMap, JPAODataPathInformation, UriInfo, Integer, JPACountQuery, EntityManager)}
   * instead *
   */
  @Deprecated(since = "2.1.0", forRemoval = true)
  default JPAODataPage getFirstPage(final UriInfo uriInfo, @Nullable final Integer preferredPageSize,
      final JPACountQuery countQuery, final EntityManager em) throws ODataApplicationException {
    return getFirstPage(null, null, uriInfo, preferredPageSize, countQuery, em).orElse(null);
  }

  /**
   * Based on the query the provider decides if a paging is required and return the first page.
   * @param requestParameter The parameter from the request context
   * @param pathInformation Request URI split info different segments like it is expected by the Olingo URI parser
   * @param uriInfo
   * @param preferredPageSize Value of the odata.maxpagesize preference header
   * @param countQuery A query that can be used to determine the maximum number of results that can be
   * expected. Only if the number of expected results is bigger then the page size a next link
   * @param em An instance of the entity manager
   * @return An optional of the page that shall be read. In case the optional is empty, all records are read from the
   * database.
   * @throws ODataApplicationException
   */
  default Optional<JPAODataPage> getFirstPage(final JPARequestParameterMap requestParameter,
      final JPAODataPathInformation pathInformation, final UriInfo uriInfo, @Nullable final Integer preferredPageSize,
      final JPACountQuery countQuery, final EntityManager em) throws ODataApplicationException {
    return Optional.ofNullable(getFirstPage(uriInfo, preferredPageSize, countQuery, em));// NOSONAR
  }

  /**
   * Requires module {@code odata-jpa-processor-cb}
   * @param requestParameter The parameter from the request context
   * @param pathInformation
   * @param uriInfo
   * @param association
   * @param preferredPageSize
   * @param count
   * @param em An instance of the entity manager
   * @return
   */
  default Optional<JPAODataExpandPage> getFirstPageExpand(final JPARequestParameterMap requestParameter,
      final JPAODataPathInformation pathInformation, final UriInfoResource uriInfo, final TopOption top,
      final SkipOption skip, final JPAAssociationAttribute association, final JPAExpandCountQuery count,
      final EntityManager em) throws ODataApplicationException {
    return Optional.empty();
  }

}
