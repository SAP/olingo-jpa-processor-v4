package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.uri.JPASkipOptionImpl;
import com.sap.olingo.jpa.processor.core.uri.JPATopOptionImpl;
import com.sap.olingo.jpa.processor.core.uri.JPAUriResourceNavigationImpl;

// TODO In case of second level $expand expandItem.getResourcePath() returns an empty UriInfoResource => Bug or
// Feature?
final class JPAExpandLevelWrapper implements JPAExpandItem, JPAExpandItemPageable {
  private final ExpandOption option;
  private final ExpandItem item;
  private final JPAEntityType jpaEntityType;
  private final LevelsExpandOption levelOptions;
  private final EdmNavigationProperty navigationPath;
  private Optional<JPAODataExpandPage> page;

  JPAExpandLevelWrapper(final JPAServiceDocument sd, final ExpandOption option, final ExpandItem item)
      throws ODataApplicationException {

    super();
    this.option = option;
    this.item = item;
    this.levelOptions = determineLevel();
    this.navigationPath = null;
    this.page = Optional.empty();
    try {
      this.jpaEntityType = sd.getEntity(Utility.determineTargetEntityType(getUriResourceParts()));
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN,
          HttpStatusCode.BAD_REQUEST, e, Utility.determineTargetEntityType(getUriResourceParts()).getName());
    }
  }

  /**
   * Special constructor to handle ..?$expand=*($levels=2) requests. This request come with a problem. They do not have
   * a resource list within the expand item. This needs to be build up from the path.
   * @param option
   * @param jpaEntityType
   * @param edmNavigationProperty
   */
  JPAExpandLevelWrapper(final ExpandOption option, final JPAEntityType jpaEntityType,
      final EdmNavigationProperty edmNavigationProperty, final ExpandItem item) {
    this.option = option;
    this.item = item;
    this.levelOptions = determineLevel();
    this.jpaEntityType = jpaEntityType;
    this.navigationPath = edmNavigationProperty;
    this.page = Optional.empty();
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return Collections.emptyList();
  }

  @Override
  public ExpandOption getExpandOption() {
    if (levelOptions.getValue() > 1 || levelOptions.isMax())
      return new ExpandOptionWrapper(option, this, item);
    else if ((levelOptions.getValue() == 1
        && item.getExpandOption() != null
        && !item.getExpandOption().getExpandItems().isEmpty()))
      return new ExpandOptionWrapper(option, this, item);
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
    return item.getResourcePath() != null ? item.getResourcePath().getUriResourceParts() : buildResourceList();
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

  private LevelsExpandOption determineLevel() {
    return item.getLevelsOption();
  }

  private List<UriResource> buildResourceList() {
    if (navigationPath != null)
      return Collections.singletonList(new JPAUriResourceNavigationImpl(navigationPath));
    return Collections.emptyList();
  }

  private class ExpandOptionWrapper implements ExpandOption {
    private final List<ExpandItem> items;
    private final ExpandOption parentOptions;

    private ExpandOptionWrapper(final ExpandOption expandOption, final UriInfoResource parentUriInfoResource,
        final ExpandItem item) {
      this.items = new ArrayList<>();
      if (item.getLevelsOption().getValue() > 1 || item.getLevelsOption().isMax())
        this.items.add(new ExpandLevelItemWrapper(item, parentUriInfoResource));
      if (item.getExpandOption() != null)
        this.items.addAll(buildAdditionalExpandItems(item));
      this.parentOptions = expandOption;
    }

    private List<? extends ExpandItem> buildAdditionalExpandItems(final ExpandItem item) {
      return item.getExpandOption().getExpandItems().stream()
          .filter(i -> i.getLevelsOption() == null)
          .toList();
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

  private class ExpandLevelItemWrapper implements ExpandItem {

    private final ExpandItem parentItem;
    private ExpandOption expandOption;
    private final LevelsExpandOption levelOption;
    private final UriInfoResource parentUriInfoResource;

    private ExpandLevelItemWrapper(final ExpandItem parentItem, final UriInfoResource parentUriInfoResource) {
      this.parentItem = parentItem;
      this.levelOption = new LevelsExpandOptionWrapper(parentItem.getLevelsOption().isMax(),
          parentItem.getLevelsOption().getValue());
      this.parentUriInfoResource = parentUriInfoResource;
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
        expandOption = new ExpandOptionWrapper(parentItem.getExpandOption(), parentUriInfoResource, parentItem);
      return expandOption;
    }

    @Override
    public UriInfoResource getResourcePath() {
      return parentItem.getResourcePath() != null ? parentItem.getResourcePath() : parentUriInfoResource;
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

    private LevelsExpandOptionWrapper(final boolean isMax, final int parentLevel) {
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

}