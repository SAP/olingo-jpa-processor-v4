package com.sap.olingo.jpa.processor.core.api.example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

public class JPAExamplePagingProvider implements JPAODataPagingProvider {

  private static final int DEFAULT_BUFFER_SIZE = 100;
  private final Map<String, Integer> maxPageSizes;
  private final Map<String, CacheEntry> pageCache;
  private final int cacheSize;
  private final Queue<String> index;

  public JPAExamplePagingProvider(Map<String, Integer> pageSizes) {
    this(pageSizes, DEFAULT_BUFFER_SIZE);
  }

  public JPAExamplePagingProvider(Map<String, Integer> pageSizes, final int bufferSize) {
    maxPageSizes = pageSizes;
    pageCache = new HashMap<>(bufferSize);
    cacheSize = bufferSize;
    index = new LinkedList<>();
  }

  @Override
  public JPAODataPage getNextPage(final String skiptoken) {
    final CacheEntry privousePage = pageCache.get(skiptoken.replaceAll("'", ""));
    if (privousePage != null) {
      // Calculate next page
      final Integer skip = privousePage.getPage().getSkip() + privousePage.getPage().getTop();
      // Create a new skiptoken if next page is not the last one
      String nextToken = null;
      if (skip + privousePage.getPage().getTop() < privousePage.getMaxTop())
        nextToken = UUID.randomUUID().toString();
      final int top = (int) ((skip + privousePage.getPage().getTop()) < privousePage.getMaxTop() ? privousePage
          .getPage().getTop() : privousePage.getMaxTop() - skip);
      final JPAODataPage page = new JPAODataPage(privousePage.getPage().getUriInfo(),
          skip, top, nextToken);
      if (nextToken != null)
        addToChach(page, privousePage.getMaxTop());
      return page;
    }
    // skiptoken not found => let JPA Processor handle this
    return null;
  }

  @Override
  public JPAODataPage getFristPage(final UriInfo uriInfo, final Integer preferedPageSize,
      final JPACountQuery countQuery, final EntityManager em) throws ODataApplicationException {

    final UriResource root = uriInfo.getUriResourceParts().get(0);
    // Paging will only be done for Entity Sets
    if (root instanceof UriResourceEntitySet) {
      // Check if Entity Set shall be packaged
      final Integer maxSize = maxPageSizes.get(((UriResourceEntitySet) root).getEntitySet().getName());
      if (maxSize != null) {
        // Read $top and $skip
        final Integer skipValue = uriInfo.getSkipOption() != null ? uriInfo.getSkipOption().getValue() : 0;
        final Integer topValue = uriInfo.getTopOption() != null ? uriInfo.getTopOption().getValue() : null;
        // Determine end of list
        final Long count = topValue != null ? (topValue + skipValue) : countQuery.countResults();
        // Determine page size
        final Integer size = preferedPageSize != null && preferedPageSize < maxSize ? preferedPageSize : maxSize;
        // Create a unique skiptoken if needed
        String skiptoken = null;
        if (size < count)
          skiptoken = UUID.randomUUID().toString();
        // Create page information
        final JPAODataPage page = new JPAODataPage(uriInfo, skipValue, topValue != null && topValue < size ? topValue
            : size, skiptoken);
        // Cache page to be able to fulfill next link based request
        if (skiptoken != null)
          addToChach(page, count);
        return page;
      }
    }
    return null;
  }

  private void addToChach(final JPAODataPage page, final Long count) {
    if (pageCache.size() == cacheSize)
      pageCache.remove(index.poll());

    pageCache.put((String) page.getSkiptoken(), new CacheEntry(count, page));
    index.add((String) page.getSkiptoken());
  }

  private class CacheEntry {
    private final Long maxTop;
    private final JPAODataPage page;

    CacheEntry(Long count, JPAODataPage page) {
      super();
      this.maxTop = count;
      this.page = page;
    }

    public Long getMaxTop() {
      return maxTop;
    }

    public JPAODataPage getPage() {
      return page;
    }
  }
}
