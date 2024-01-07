package com.sap.olingo.jpa.processor.core.api.example;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPathInformation;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

public class JPAExamplePagingProvider implements JPAODataPagingProvider {

  private static final int DEFAULT_BUFFER_SIZE = 100;
  private final Map<String, Integer> maxPageSizes;
  private final Map<String, CacheEntry> pageCache;
  private final Queue<String> index;
  private final int cacheSize;

  public JPAExamplePagingProvider(final Map<String, Integer> pageSizes) {
    this(pageSizes, DEFAULT_BUFFER_SIZE);
  }

  public JPAExamplePagingProvider(final Map<String, Integer> pageSizes, final int bufferSize) {
    maxPageSizes = Collections.unmodifiableMap(pageSizes);
    pageCache = new ConcurrentHashMap<>(bufferSize);
    cacheSize = bufferSize;
    index = new ConcurrentLinkedQueue<>();
  }

  @Override
  public Optional<JPAODataPage> getNextPage(@Nonnull final String skipToken, final OData odata,
      final ServiceMetadata serviceMetadata, final JPARequestParameterMap requestParameter, final EntityManager em) {
    final CacheEntry previousPage = pageCache.get(skipToken.replace("'", ""));
    if (previousPage != null) {
      // Calculate next page
      final Integer skip = previousPage.page().skip() + previousPage.page().top();
      // Create a new skip token if next page is not the last one
      String nextToken = null;
      if (skip + previousPage.page().top() < previousPage.maxTop())
        nextToken = UUID.randomUUID().toString();
      final int top = (int) ((skip + previousPage.page().top()) < previousPage.maxTop() ? previousPage
          .page().top() : previousPage.maxTop() - skip);
      final JPAODataPage page = new JPAODataPage(previousPage.page().uriInfo(), skip, top, nextToken);
      if (nextToken != null)
        addToCache(page, previousPage.maxTop());
      return Optional.of(page);
    }
    // skip token not found => let JPA Processor handle this by return http.gone
    return Optional.empty();
  }

  @Override
  public Optional<JPAODataPage> getFirstPage(final JPARequestParameterMap requestParameter,
      final JPAODataPathInformation pathInformation, final UriInfo uriInfo, @Nullable final Integer preferredPageSize,
      final JPACountQuery countQuery, final EntityManager em) throws ODataApplicationException {

    final UriResource root = uriInfo.getUriResourceParts().get(0);
    // Paging will only be done for Entity Sets
    if (root instanceof final UriResourceEntitySet entitySet) {
      // Check if Entity Set shall be packaged
      final Integer maxSize = maxPageSizes.get(entitySet.getEntitySet().getName());
      if (maxSize != null) {
        // Read $top and $skip
        final Integer skipValue = uriInfo.getSkipOption() != null ? uriInfo.getSkipOption().getValue() : 0;
        final Integer topValue = uriInfo.getTopOption() != null ? uriInfo.getTopOption().getValue() : null;
        // Determine page size
        final Integer pageSize = preferredPageSize != null && preferredPageSize < maxSize ? preferredPageSize : maxSize;
        if (topValue != null && topValue <= pageSize)
          return Optional.of(new JPAODataPage(uriInfo, skipValue, topValue, null));
        // Determine end of list
        final Long maxResults = countQuery.countResults();
        final Long count = topValue != null && (topValue + skipValue) < maxResults
            ? topValue.longValue() : maxResults - skipValue;
        final Long last = topValue != null && (topValue + skipValue) < maxResults
            ? (topValue + skipValue) : maxResults;
        // Create a unique skip token if needed
        String skipToken = null;
        if (pageSize < count)
          skipToken = UUID.randomUUID().toString();
        // Create page information
        final JPAODataPage page = new JPAODataPage(uriInfo, skipValue, pageSize, skipToken);
        // Cache page to be able to fulfill next link based request
        if (skipToken != null)
          addToCache(page, last);
        return Optional.of(page);
      }
    }
    return Optional.empty();
  }

  private void addToCache(final JPAODataPage page, final Long count) {
    if (pageCache.size() == cacheSize)
      pageCache.remove(index.poll());

    pageCache.put((String) page.skipToken(), new CacheEntry(count, page));
    index.add((String) page.skipToken());
  }

  private static record CacheEntry(Long maxTop, JPAODataPage page) {}
}
