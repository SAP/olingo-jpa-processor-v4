package com.sap.olingo.jpa.processor.core.uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
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
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

class JPAUriInfoResourceImpl implements JPAUriInfoResource {

  private List<UriResource> pathParts;
  private final UriInfoResource original;

  private final Map<SystemQueryOptionKind, SystemQueryOption> systemQueryOptions =
      new EnumMap<>(SystemQueryOptionKind.class);

  JPAUriInfoResourceImpl(@Nullable final UriInfoResource original) {
    if (original != null) {
      this.original = original;
      this.pathParts = new ArrayList<>(original.getUriResourceParts());
      copySystemQueryOptions();
    } else {
      this.original = null;
      this.pathParts = new ArrayList<>();
    }
  }

  JPAUriInfoResourceImpl(@Nullable final UriInfoResource original, final List<UriResourcePartTyped> list) {
    this.original = original;
    this.pathParts = new ArrayList<>(list);
    if (original != null)
      copySystemQueryOptions();
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return original.getCustomQueryOptions();
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return Collections.unmodifiableList(pathParts);
  }

  @Override
  public String getValueForAlias(final String alias) {
    return original.getValueForAlias(alias);
  }

  @Override
  public ExpandOption getExpandOption() {
    return (ExpandOption) systemQueryOptions.get(SystemQueryOptionKind.EXPAND);
  }

  @Override
  public FilterOption getFilterOption() {
    return (FilterOption) systemQueryOptions.get(SystemQueryOptionKind.FILTER);
  }

  @Override
  public FormatOption getFormatOption() {
    return (FormatOption) systemQueryOptions.get(SystemQueryOptionKind.FORMAT);
  }

  @Override
  public IdOption getIdOption() {
    return (IdOption) systemQueryOptions.get(SystemQueryOptionKind.ID);
  }

  @Override
  public CountOption getCountOption() {
    return (CountOption) systemQueryOptions.get(SystemQueryOptionKind.COUNT);
  }

  @Override
  public OrderByOption getOrderByOption() {
    return (OrderByOption) systemQueryOptions.get(SystemQueryOptionKind.ORDERBY);
  }

  @Override
  public SearchOption getSearchOption() {
    return (SearchOption) systemQueryOptions.get(SystemQueryOptionKind.SEARCH);
  }

  @Override
  public SelectOption getSelectOption() {
    return (SelectOption) systemQueryOptions.get(SystemQueryOptionKind.SELECT);
  }

  @Override
  public SkipOption getSkipOption() {
    return (SkipOption) systemQueryOptions.get(SystemQueryOptionKind.SKIP);
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return (SkipTokenOption) systemQueryOptions.get(SystemQueryOptionKind.SKIPTOKEN);
  }

  @Override
  public TopOption getTopOption() {
    return (TopOption) systemQueryOptions.get(SystemQueryOptionKind.TOP);
  }

  @Override
  public ApplyOption getApplyOption() {
    return (ApplyOption) systemQueryOptions.get(SystemQueryOptionKind.APPLY);
  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return (DeltaTokenOption) systemQueryOptions.get(SystemQueryOptionKind.DELTATOKEN);
  }

  @Override
  public UriResource getLastResourcePart() {
    return pathParts.get(pathParts.size() - 1);
  }

  void setSystemQueryOption(final SystemQueryOption option) {
    systemQueryOptions.put(option.getKind(), option);
  }

  void removeSystemQueryOption(final SystemQueryOptionKind kind) {
    systemQueryOptions.remove(kind);
  }

  void setUriResourceParts(final List<UriResource> newResourceParts) {
    pathParts = newResourceParts;
  }

  public void addUriResourceParts(final UriResource resource) {
    pathParts.add(resource);
  }

  private void copySystemQueryOptions() {
    copySystemQueryOption(SystemQueryOptionKind.EXPAND, original.getExpandOption());
    copySystemQueryOption(SystemQueryOptionKind.FILTER, original.getFilterOption());
    copySystemQueryOption(SystemQueryOptionKind.FORMAT, original.getFormatOption());
    copySystemQueryOption(SystemQueryOptionKind.ID, original.getIdOption());
    copySystemQueryOption(SystemQueryOptionKind.COUNT, original.getCountOption());
    copySystemQueryOption(SystemQueryOptionKind.DELTATOKEN, original.getDeltaTokenOption());
    copySystemQueryOption(SystemQueryOptionKind.ORDERBY, original.getOrderByOption());
    copySystemQueryOption(SystemQueryOptionKind.SEARCH, original.getSearchOption());
    copySystemQueryOption(SystemQueryOptionKind.SELECT, original.getSelectOption());
    copySystemQueryOption(SystemQueryOptionKind.SKIP, original.getSkipOption());
    copySystemQueryOption(SystemQueryOptionKind.SKIPTOKEN, original.getSkipTokenOption());
    copySystemQueryOption(SystemQueryOptionKind.TOP, original.getTopOption());
    copySystemQueryOption(SystemQueryOptionKind.APPLY, original.getApplyOption());
  }

  private void copySystemQueryOption(final SystemQueryOptionKind kind, final SystemQueryOption systemQueryOption) {
    if (systemQueryOption != null)
      systemQueryOptions.put(kind, systemQueryOption);
  }
}
