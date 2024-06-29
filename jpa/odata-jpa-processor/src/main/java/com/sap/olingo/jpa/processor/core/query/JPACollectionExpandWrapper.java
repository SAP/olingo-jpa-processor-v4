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
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public class JPACollectionExpandWrapper implements JPAExpandItem {
  private final JPAEntityType jpaEntityType;
  private final UriInfoResource uriInfo;
  private final SelectOption selectOption;

  JPACollectionExpandWrapper(final JPAEntityType jpaEntityType, final UriInfoResource uriInfo,
      final JPAAssociationPath associationPath) {
    super();
    this.jpaEntityType = jpaEntityType;
    this.uriInfo = uriInfo;
    this.selectOption = buildSelectOption(uriInfo.getSelectOption(), associationPath);
  }

  public JPACollectionExpandWrapper(final JPAEntityType jpaEntityType, final UriInfoResource uriInfo) {
    this.jpaEntityType = jpaEntityType;
    this.uriInfo = uriInfo;
    this.selectOption = uriInfo.getSelectOption();
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return new ArrayList<>(0);
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
    return selectOption;
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
  public String getValueForAlias(final String alias) {
    return null;
  }

  @Override
  public JPAEntityType getEntityType() {
    return jpaEntityType;
  }

  private SelectOption buildSelectOption(final SelectOption selectOption, final JPAAssociationPath associationPath) {
    final JPACollectionAttribute collectionAttribute = (JPACollectionAttribute) associationPath.getLeaf();
    final List<SelectItem> itemsOfProperty = selectOption.getSelectItems().stream()
        .filter(item -> containsCollectionProperty(collectionAttribute, item.getResourcePath()))
        .toList();
    return new SelectOptionImpl(selectOption.getKind(), selectOption.getName(), selectOption.getText(),
        itemsOfProperty);
  }

  private boolean containsCollectionProperty(final JPACollectionAttribute collectionAttribute,
      final UriInfoResource resourcePath) {
    return resourcePath.getUriResourceParts().stream()
        .anyMatch(part -> part.getSegmentValue().equals(collectionAttribute.getExternalName()));
  }

  private static class SelectOptionImpl implements SelectOption {

    private final SystemQueryOptionKind kind;
    private final String name;
    private final String text;
    private final List<SelectItem> items;

    public SelectOptionImpl(final SystemQueryOptionKind kind, final String name, final String text,
        final List<SelectItem> itemsOfProperty) {
      this.kind = kind;
      this.name = name;
      this.text = text;
      this.items = itemsOfProperty;
    }

    @Override
    public SystemQueryOptionKind getKind() {
      return kind;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public List<SelectItem> getSelectItems() {
      return items;
    }

  }

}
