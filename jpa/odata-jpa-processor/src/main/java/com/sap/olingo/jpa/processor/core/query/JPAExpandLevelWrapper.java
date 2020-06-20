package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

// TODO In case of second level $expand expandItem.getResourcePath() returns an empty UriInfoResource => Bug or
// Feature?
public final class JPAExpandLevelWrapper implements JPAExpandItem {
  private final ExpandOption option;
  private final ExpandItem item;
  private final JPAEntityType jpaEntityType;
  private final LevelsExpandOption levelOptions;

  public JPAExpandLevelWrapper(final JPAServiceDocument sd, final ExpandOption option)
      throws ODataApplicationException {

    super();
    this.option = option;
    this.item = option.getExpandItems().get(0);
    this.levelOptions = determineLevel(item);
    try {
      this.jpaEntityType = sd.getEntity(Util.determineTargetEntityType(getUriResourceParts()));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN,
          HttpStatusCode.BAD_REQUEST, e, Util.determineTargetEntityType(getUriResourceParts()).getName());
    }
  }

  public JPAExpandLevelWrapper(final ExpandOption option, final JPAEntityType jpaEntityType) {

    super();
    this.option = option;
    this.item = option.getExpandItems().get(0);
    this.levelOptions = determineLevel(item);
    this.jpaEntityType = jpaEntityType;
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return null;
  }

  @Override
  public ExpandOption getExpandOption() {
    if (levelOptions.getValue() > 1 || levelOptions.isMax())
      return new ExpandOptionWrapper(option);
    else
      return null;
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
    return item.getSkipOption();
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return null;
  }

  @Override
  public TopOption getTopOption() {
    return item.getTopOption();
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return item.getResourcePath() != null ? item.getResourcePath().getUriResourceParts() : Collections.emptyList();
  }

  @Override
  public String getValueForAlias(final String alias) {
    return null;
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  @Override
  public ApplyOption getApplyOption() {
    return null;
  }

  private LevelsExpandOption determineLevel(ExpandItem item2) {
    return item.getLevelsOption();
  }

  private class ExpandOptionWrapper implements ExpandOption {
    private final List<ExpandItem> items;
    private final ExpandOption parentOptions;

    private ExpandOptionWrapper(ExpandOption expandOption) {
      this.items = new ArrayList<>();
      this.items.add(new ExpandItemWrapper(expandOption.getExpandItems().get(0)));
      this.parentOptions = expandOption;
      expandOption.getExpandItems().get(0).getLevelsOption();
    }

    @Override
    public SystemQueryOptionKind getKind() {
      return parentOptions.getKind();
    }

    @Override
    public String getName() {
      return parentOptions.getName();
    }

    @Override
    public String getText() {
      return parentOptions.getText();
    }

    @Override
    public List<ExpandItem> getExpandItems() {
      return items;
    }
  }

  private class ExpandItemWrapper implements ExpandItem {

    private final ExpandItem parentItem;
    private ExpandOption expandOption;
    private final LevelsExpandOption levelOption;

    private ExpandItemWrapper(ExpandItem parentItem) {
      this.parentItem = parentItem;
      this.levelOption = new LevelsExpandOptionWrapper(parentItem.getLevelsOption().isMax(),
          parentItem.getLevelsOption().getValue());
    }

    @Override
    public LevelsExpandOption getLevelsOption() {
      return levelOption;
    }

    @Override
    public FilterOption getFilterOption() {
      return parentItem.getFilterOption();
    }

    @Override
    public SearchOption getSearchOption() {
      return null;
    }

    @Override
    public OrderByOption getOrderByOption() {
      return parentItem.getOrderByOption();
    }

    @Override
    public SkipOption getSkipOption() {
      return parentItem.getSkipOption();
    }

    @Override
    public TopOption getTopOption() {
      return parentItem.getTopOption();
    }

    @Override
    public CountOption getCountOption() {
      return parentItem.getCountOption();
    }

    @Override
    public SelectOption getSelectOption() {
      return parentItem.getSelectOption();
    }

    @Override
    public ExpandOption getExpandOption() {
      if (expandOption == null)
        expandOption = new ExpandOptionWrapper(parentItem.getExpandOption());
      return expandOption;
    }

    @Override
    public UriInfoResource getResourcePath() {
      return parentItem.getResourcePath();
    }

    @Override
    public boolean isStar() {
      return false;
    }

    @Override
    public boolean isRef() {
      return false;
    }

    @Override
    public boolean hasCountPath() {
      return false;
    }

    @Override
    public EdmType getStartTypeFilter() {
      return parentItem.getStartTypeFilter();
    }

    @Override
    public ApplyOption getApplyOption() {
      return null;
    }
  }

  private static class LevelsExpandOptionWrapper implements LevelsExpandOption {
    private final boolean isMax;
    private final int level;

    private LevelsExpandOptionWrapper(boolean isMax, int parentLevel) {
      super();
      this.isMax = isMax;
      if (parentLevel != 0)
        this.level = parentLevel - 1;
      else
        this.level = 0;
    }

    @Override
    public boolean isMax() {
      return isMax;
    }

    @Override
    public int getValue() {
      return level;
    }

  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return null;
  }
}