package com.sap.olingo.jpa.processor.core.uri;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

class JPAExpandItem implements ExpandItem {

  private final ExpandItem original;
  private final LevelsExpandOption levelsOption;
  private final UriInfoResource uriInfo;
  private final ExpandOption expandOption;
  private final boolean isStar;

  JPAExpandItem(@Nonnull final ExpandItem item, final LevelsExpandOption levelsOption) {
    this.original = item;
    this.levelsOption = levelsOption;
    this.uriInfo = item.getResourcePath();
    this.expandOption = original.getExpandOption();
    this.isStar = item.isStar();
  }

  JPAExpandItem(@Nonnull final ExpandItem item, final List<UriResourcePartTyped> navigation,
      final LevelsExpandOption levels) {
    this.original = item;
    this.levelsOption = null;
    if (levels != null
        && (levels.getValue() > 0 || levels.isMax()))
      this.expandOption = new JPAExpandOption(levels);
    else
      this.expandOption = null;
    this.uriInfo = new JPAUriInfoResourceImpl(item.getResourcePath(), navigation.subList(1, navigation.size()));
    this.isStar = false;
  }

  public JPAExpandItem(final ExpandItem item, final ExpandOption expandOption) {
    this.original = item;
    this.levelsOption = item.getLevelsOption();
    this.uriInfo = item.getResourcePath();
    this.expandOption = expandOption;
    this.isStar = item.isStar();
  }

  public JPAExpandItem(final LevelsExpandOption levels) {
    this.original = null;
    this.levelsOption = levels;
    this.uriInfo = new JPAUriInfoResourceImpl(null);
    this.expandOption = null;
    this.isStar = true;
  }

  @Override
  public LevelsExpandOption getLevelsOption() {
    return levelsOption;
  }

  @Override
  public FilterOption getFilterOption() {
    return original == null ? null : original.getFilterOption();
  }

  @Override
  public SearchOption getSearchOption() {
    return original == null ? null : original.getSearchOption();
  }

  @Override
  public OrderByOption getOrderByOption() {
    return original == null ? null : original.getOrderByOption();
  }

  @Override
  public SkipOption getSkipOption() {
    return original == null ? null : original.getSkipOption();
  }

  @Override
  public TopOption getTopOption() {
    return original == null ? null : original.getTopOption();
  }

  @Override
  public CountOption getCountOption() {
    return original == null ? null : original.getCountOption();
  }

  @Override
  public SelectOption getSelectOption() {
    return original == null ? null : original.getSelectOption();
  }

  @Override
  public ExpandOption getExpandOption() {
    return expandOption;
  }

  @Override
  public ApplyOption getApplyOption() {
    return original == null ? null : original.getApplyOption();
  }

  /**
   * Path of the $expand. E.g. Children in case of .../AdministrativeDivisions?$expand=Children. It is empty for
   * $expand=*
   * @return
   */
  @Override
  public UriInfoResource getResourcePath() {
    return uriInfo;
  }

  @Override
  public boolean isStar() {
    return isStar;
  }

  @Override
  public boolean isRef() {
    return original != null && original.isRef();
  }

  @Override
  public boolean hasCountPath() {
    return original != null && original.hasCountPath();
  }

  @Override
  public EdmType getStartTypeFilter() {
    return original == null ? null : original.getStartTypeFilter();
  }

}
