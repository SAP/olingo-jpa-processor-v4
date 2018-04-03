package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
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

public class JPACollectionExpandWrapper implements JPAExpandItem {
  private final JPAEntityType jpaEntityType;
  private final UriInfoResource uriInfo;

  JPACollectionExpandWrapper(final JPAEntityType jpaEntityType, final UriInfoResource uriInfo) {
    super();
    this.jpaEntityType = jpaEntityType;
    this.uriInfo = uriInfo;
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return new ArrayList<>(1);
  }

  @Override
  public ExpandOption getExpandOption() {
    return null;
  }

  @Override
  public FilterOption getFilterOption() {
    return uriInfo.getFilterOption();
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
    return uriInfo.getCountOption();
  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return null;
  }

  @Override
  public OrderByOption getOrderByOption() {
    return null;
  }

  @Override
  public SearchOption getSearchOption() {
    return null;
  }

  @Override
  public SelectOption getSelectOption() {
    return uriInfo.getSelectOption();
  }

  @Override
  public SkipOption getSkipOption() {
    return null;
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return null;
  }

  @Override
  public TopOption getTopOption() {
    return null;
  }

  @Override
  public ApplyOption getApplyOption() {
    return null;
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriInfo.getUriResourceParts();
  }

  @Override
  public String getValueForAlias(String alias) {
    return null;
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

}
