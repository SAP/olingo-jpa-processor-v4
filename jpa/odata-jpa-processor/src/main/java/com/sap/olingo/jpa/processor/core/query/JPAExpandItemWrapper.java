package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.uri.JPASkipOptionImpl;
import com.sap.olingo.jpa.processor.core.uri.JPATopOptionImpl;

// TODO In case of second level $expand expandItem.getResourcePath() returns an empty UriInfoResource => Bug or
// Feature?
public class JPAExpandItemWrapper implements JPAExpandItem, JPAExpandItemPageable {
  private final ExpandItem item;
  private final JPAEntityType jpaEntityType;
  private Optional<JPAODataExpandPage> page;
  private final List<UriResource> uriResourceParts;

  public JPAExpandItemWrapper(final JPAServiceDocument sd, final ExpandItem item) throws ODataApplicationException {
    super();
    this.item = item;
    this.uriResourceParts = item.getResourcePath() != null
        ? item.getResourcePath().getUriResourceParts()
        : Collections.emptyList();
    this.page = Optional.empty();

    try {
      this.jpaEntityType = sd.getEntity(Utility.determineTargetEntityType(getUriResourceParts()));
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN,
          HttpStatusCode.BAD_REQUEST, e, Utility.determineTargetEntityType(getUriResourceParts()).getName());
    }
  }

  public JPAExpandItemWrapper(final ExpandItem item, final JPAEntityType jpaEntityType) {
    super();
    this.item = item;
    this.jpaEntityType = jpaEntityType;
    this.uriResourceParts = item.getResourcePath() != null
        ? item.getResourcePath().getUriResourceParts()
        : Collections.emptyList();
    this.page = Optional.empty();
  }

  public JPAExpandItemWrapper(final ExpandItem item, final JPAEntityType jpaEntityType,
      final UriResourcePartTyped uriResource) {
    super();
    this.item = item;
    this.jpaEntityType = jpaEntityType;
    this.uriResourceParts = Collections.singletonList(uriResource);
    this.page = Optional.empty();
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return Collections.emptyList();
  }

  @Override
  public ExpandOption getExpandOption() {
    return item.getExpandOption();
  }

  @Override
  public FilterOption getFilterOption() {
    return item.getFilterOption();
  }

  @Override
  public FormatOption getFormatOption() {
    return null;
  }

  @Override
  public IdOption getIdOption() {
    return null;
  }

  @Override
  public CountOption getCountOption() {
    return item.getCountOption();
  }

  @Override
  public OrderByOption getOrderByOption() {
    return item.getOrderByOption();
  }

  @Override
  public SearchOption getSearchOption() {
    return item.getSearchOption();
  }

  @Override
  public SelectOption getSelectOption() {
    return item.getSelectOption();
  }

  @Override
  public SkipOption getSkipOption() {
    if (page.isPresent())
      return new JPASkipOptionImpl(page.get().skip());
    return item.getSkipOption();
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return null;
  }

  @Override
  public TopOption getTopOption() {
    if (page.isPresent())
      return new JPATopOptionImpl(page.get().top());
    return item.getTopOption();
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriResourceParts;
  }

  @Override
  public String getValueForAlias(final String alias) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.query.JPAExpandItem#getEntityType()
   */
  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  @Override
  public ApplyOption getApplyOption() {
    return null;
  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return null;
  }

  @Override
  public Optional<JPAODataSkipTokenProvider> getSkipTokenProvider() {
    if (page.isPresent())
      return Optional.ofNullable(page.get().skipToken());
    return Optional.empty();
  }

  @Override
  public void setPage(final JPAODataExpandPage page) {
    this.page = Optional.ofNullable(page);
  }

}